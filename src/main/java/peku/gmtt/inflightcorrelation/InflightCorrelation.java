package peku.gmtt.inflightcorrelation;

import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.internals.KTableAggregate;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.log4j.Logger;

import peku.gmtt.model.GMTTEvent;
import peku.gmtt.model.GMTTEventOnlyIDs;
import peku.serializer.JsonDeserializer;
import peku.serializer.JsonSerializer;
//import java.io.FileReader;
//import java.util.Iterator;

//import com.fasterxml.jackson.databind.JsonNode;
//import org.apache.kafka.connect.json.JsonDeserializer;
//import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.kstream.ValueJoiner;

import java.util.Properties;
import java.util.UUID;

public class InflightCorrelation {
    // Log4J
    final static Logger logger = Logger.getLogger(InflightCorrelation.class);
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static Boolean running = false;

    public static void main(String[] args) {

        // InflightCorrelation obj = new InflightCorrelation();
        logger.info("InflightCorrelation started.");

        // Connect to Kafka broker as given as parameter. If not given, default to localhost
        final String bootstrapServers = args.length == 1 ? args[0] : DEFAULT_BOOTSTRAP_SERVERS;

        //2. Launch Kafka Streams Topology
        KafkaStreams streams = createEventCorrelationStreamsInstance(bootstrapServers);

        try {
            logger.info("About to start the streams instance");
            // streams.cleanUp();  // Forcefully reset local state
            streams.start();    // Start the processing
            running = true;
        } catch (Throwable e) {
            logger.error("Stopping the application due to streams initialization error ", e);
            // connect.stop();
        }

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
//        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("SIGTERM received. About to close application.");
            streams.close();
            running = false;
            logger.info("Streams closed cleanly...");
        }));

        logger.info("InflightCorrelation running until SIGTERM is sent.");
        try {
            while (running) {
                Thread.sleep(10000);
                logger.info("Loop to keep programme active... another 10 seconds have passed.");
            }
            //connect.awaitStop();
            //log.info("Connect closed cleanly...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            streams.close();
            logger.info("Streams closed cleanly...");
        }
    }

    static KafkaStreams createEventCorrelationStreamsInstance(String bootstrapServers) {
        // Set properties for the stream processor
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "gmtteventcorrelation");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        //Serializers for types used in the processors
        JsonDeserializer<GMTTEvent> gmtteventJsonDeserializer = new JsonDeserializer<>(GMTTEvent.class);
        JsonSerializer<GMTTEvent> gmtteventJsonSerializer = new JsonSerializer<>();
        Serde<GMTTEvent> gmtteventSerde = Serdes.serdeFrom(gmtteventJsonSerializer, gmtteventJsonDeserializer);

        JsonDeserializer<GMTTEventOnlyIDs> gmtteventOnlyIDsJsonDeserializer = new JsonDeserializer<>(GMTTEventOnlyIDs.class);
        JsonSerializer<GMTTEventOnlyIDs> gmtteventOnlyIDsJsonSerializer = new JsonSerializer<>();
        Serde<GMTTEventOnlyIDs> gmtteventOnlyIDsSerde = Serdes.serdeFrom(gmtteventOnlyIDsJsonSerializer, gmtteventOnlyIDsJsonDeserializer);

        StringDeserializer stringDeserializer = new StringDeserializer();
        StringSerializer stringSerializer = new StringSerializer();
        Serde<String> stringSerde = Serdes.serdeFrom(stringSerializer, stringDeserializer);

        // Strategy:
        // 1. Read each event from the input stream into KStream 'eventKStream' and pick only the essential identifiers for further processing
        //
        // 2. Create KTable 'componentIdByLocalKStream'. This one carries all known componentIDs (value) per localComponentId (key)
        //    Using the KStream with all events put all combinations of localComponentId and ComponentId for the lookup
        // TODO Apply sliding window on the KTable (we cannot keep a history since the beginning of time...)
        // NOTE: what to do if for a localComponentId a 2nd event is encountered that does not have the same ComponentId as the 1st one? Can this happen and is it a valid case?

        // 3. Create KTable 'ComponentIDbyExternalID'. This one carries all known componentIDs (value) per externalComponentId (key)
        //    Using the KStream with all events select combinations of localComponentId and ComponentId
        // TODO Apply sliding window on the KTable (we cannot keep a history since the beginning of time...)

        // Step 4: Create KStream 'eventByExternalKStream' with events that carry an externalComponentId but NOT a componentId (i.e. events that we aim to enrich)
        //         Key is the externalComponentId
        // Step 5: Join events without componentId with table with componentIds using the externalComponentId
        // Step 6: Create KStream 'eventByLocalKStream' with events that carry a localComponentId but NOT a componentId (i.e. events that we aim to enrich)
        //         Key is the localComponentId


        // Step 7: Join events without componentId with table with componentIds using the localComponentId (this is 1 of 4 possible joins)


        // Build a Stream Topology using DSL
        KStreamBuilder builder = new KStreamBuilder();

        // Step 1: Read the source stream
        KStream<String, GMTTEvent> eventKStream = builder.stream(stringSerde, gmtteventSerde, "gmtt-events");

        // Step 1: Create a KStream as 'light' as possible with only the IDs that are relevant for correlating and enriching of events
        KStream<String, GMTTEventOnlyIDs> eventOnlyIDsKStream = eventKStream
                .mapValues(new ValueMapper<GMTTEvent, GMTTEventOnlyIDs>() {
                    @Override
                    public GMTTEventOnlyIDs apply(GMTTEvent gmttEvent) {
                        // We are only interested in identifiers, so only those are written to the stream
                        // TODO: Add logic to work around data quality issues such
                        // TODO: empty strings as identifiers (done)
                        // TODO: special characters such as line feeds
                        GMTTEventOnlyIDs onlyIDs = new GMTTEventOnlyIDs();

                        if (gmttEvent.getComponents().get(0).getLocalComponentID() != null) {
                            if (gmttEvent.getComponents().get(0).getLocalComponentID().length() > 0) {
                                onlyIDs.setLocalComponentId(gmttEvent.getComponents().get(0).getLocalComponentID());
                            }
                        }
                        if (gmttEvent.getComponents().get(0).getComponentID() != null) {
                            if (gmttEvent.getComponents().get(0).getComponentID().length() > 0) {
                                onlyIDs.setComponentId(gmttEvent.getComponents().get(0).getComponentID());
                            }
                        }
                        if (gmttEvent.getComponents().get(0).getExternalComponentID() != null) {
                            if (gmttEvent.getComponents().get(0).getExternalComponentID().length() > 0) {
                                onlyIDs.setExternalComponentId(gmttEvent.getComponents().get(0).getExternalComponentID());
                            }
                        }
                        // Forcefully set a UUID as eventID
                        onlyIDs.setEventId(UUID.randomUUID().toString());
                        //onlyIDs.setEventId( gmttEvent.getEventID());

                        logger.info("Step 1: Applied mapping of ID values");
                        //logger.info("Step 1: value of localComponentId:"+ onlyIDs.getLocalComponentId());
                        //logger.info("Step 1: value of externalComponentId:"+ onlyIDs.getExternalComponentId());
                        //logger.info("Step 1: value of componentId:"+ onlyIDs.getComponentId());
                        //logger.info("Step 1: value of eventId:"+ onlyIDs.getEventId());
                        //logger.info("Step 1: dump of gmttEvent:" + gmttEvent.toString());
                        return onlyIDs;
                    }
                })
               ;

        // 2. Create KTable 'componentIdByLocalKStream'. This one carries all known componentIDs (value) per localComponentId (key)
        //    Using the KStream with all events select combinations of localComponentId and ComponentId
        eventOnlyIDsKStream
                .filter(new Predicate<String, GMTTEventOnlyIDs>() {
                    @Override
                    public boolean test(String s, GMTTEventOnlyIDs gmttEventOnlyIDs) {
                        // Looking for events with both a localComponentId AND a componentId
                        return (gmttEventOnlyIDs.getLocalComponentId() != null) & (gmttEventOnlyIDs.getComponentId() != null);
                    }
                })
                .map(new KeyValueMapper<String, GMTTEventOnlyIDs, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(String s, GMTTEventOnlyIDs gmttEvent) {
                        logger.info("Step 2: Key/Value mapped for lookup componentid by local:"+gmttEvent.getLocalComponentId()+"]*["+gmttEvent.getComponentId());
                        return new KeyValue<>(gmttEvent.getLocalComponentId(), gmttEvent.getComponentId());
                    }
                })
                // Write k/v to topic specifically for purpose of creating a lookup table backed by a statestore (next step)
                .to(stringSerde, stringSerde, "gmtt-componentid-by-local");

        // Read topic into KTable which will function as the lookup table
        KTable<String, String> ComponentIDbyLocalID = builder.table(stringSerde, stringSerde, "gmtt-componentid-by-local","lookup-componentid-by-localid");

        // 3. Create KTable 'ComponentIDbyExternalID'. This one carries all known componentIDs (value) per externalComponentId (key)
        //    Using the KStream with all events select combinations of localComponentId and ComponentId
/* (not used for now, because externalComponentID as a lookup key is not likely
        eventOnlyIDsKStream
                // TODO: Add sliding window
                .filter(new Predicate<String, GMTTEventOnlyIDs>() {
                    @Override
                    public boolean test(String s, GMTTEventOnlyIDs gmttEventOnlyIDs) {
                        // Looking for events with both a localComponentId AND a componentId
                        return (gmttEventOnlyIDs.getExternalComponentId() != null) & (gmttEventOnlyIDs.getComponentId() != null);
                    }
                })
                .map(new KeyValueMapper<String, GMTTEventOnlyIDs, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(String s, GMTTEventOnlyIDs gmttEvent) {
                        logger.info("Step 3: Key/Value mapped for lookup componentid by external");
                        return new KeyValue<>(gmttEvent.getExternalComponentId(), gmttEvent.getComponentId());
                    }
                })
                // Write k/v to topic specifically for purpose of creating a lookup table backed by a statestore (next step)
                .to(stringSerde, stringSerde, "gmtt-componentid-by-external");

        // Read topic into KTable which will function as the lookup table
        KTable<String, String> ComponentIDbyExternalID = builder.table(stringSerde, stringSerde, "gmtt-componentid-by-external","lookup-componentid-by-externalid");
*/
        // Step 4: Create KStream 'eventByExternalKStream' with events that carry an externalComponentId but NOT a componentId (i.e. events that we aim to enrich)
        //         Key is the externalComponentId
        KStream<String, GMTTEventOnlyIDs> eventByExternalKStream = eventOnlyIDsKStream
                .filter(new Predicate<String, GMTTEventOnlyIDs>() {
                    @Override
                    public boolean test(String s, GMTTEventOnlyIDs gmttEventOnlyIDs) {
                        // If no externalComponentId is present, filter out the record from the stream
                        // If event already has a ComponentID, then filter it out
                        return (gmttEventOnlyIDs.getExternalComponentId() != null) & (gmttEventOnlyIDs.getComponentId() == null);
                    }
                })
                .map(new KeyValueMapper<String, GMTTEventOnlyIDs, KeyValue<String, GMTTEventOnlyIDs>>() {
                    @Override
                    public KeyValue<String, GMTTEventOnlyIDs> apply(String s, GMTTEventOnlyIDs gmttEvent) {
                        logger.info("Step 4: To be enriched event found. ExternalComponentId:"+ gmttEvent.getExternalComponentId() + " EventId:"+ gmttEvent.getEventId());

                        return new KeyValue<>(gmttEvent.getExternalComponentId(), gmttEvent);
                    }
                })
                // For debugging puposes: write to topic
                // NOTE: only by explicitly writing to a topic the Serdes can be enforced (or so it seems)
                .through(stringSerde, gmtteventOnlyIDsSerde, "gmtt-events-by-external")
                ;

        // Step 5: Join events without componentId with table with componentIds using the externalComponentId (this is 1 of 4 possible joins)
        KStream<String, String> joinedLocalExternal = eventByExternalKStream
                .join(ComponentIDbyLocalID, new ValueJoiner<GMTTEventOnlyIDs, String, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(GMTTEventOnlyIDs gmttEvent, String componentID) {
                        logger.info("Step 5: Enrichment found LocalExternal: " + gmttEvent.getEventId() + "]*[" + componentID);
                        // The outcome of join is a value that consists of a keyvalue pair that will be mapped to the key and the value of the output stream in the next step
                        return new KeyValue<>(gmttEvent.getEventId(), componentID);
                    }
                })
                .map(new KeyValueMapper<String, KeyValue<String, String>, KeyValue<String, String>>() {
                    @Override
                    // The keyvalue pair that is the joined value of the above join is taken apart in key and value of the output stream
                    public KeyValue<String, String> apply(String k, KeyValue<String, String> eventIdComponentId) {
                        return new KeyValue<>(eventIdComponentId.key, eventIdComponentId.value);
                    }
                })
                ;
        joinedLocalExternal.to(stringSerde, stringSerde,"gmtt-enriched-events");

        // Step 6: Create KStream 'eventByLocalKStream' with events that carry a localComponentId but NOT a componentId (i.e. events that we aim to enrich)
        //         Key is the localComponentId
        KStream<String, GMTTEventOnlyIDs> eventByLocalKStream = eventOnlyIDsKStream
                .filter(new Predicate<String, GMTTEventOnlyIDs>() {
                    @Override
                    public boolean test(String s, GMTTEventOnlyIDs gmttEventOnlyIDs) {
                        // If no localComponentId is present, filter out the record from the stream
                        // If event already has a ComponentID, then filter it out
                        return (gmttEventOnlyIDs.getLocalComponentId() != null) & (gmttEventOnlyIDs.getComponentId() == null);
                    }
                })
                .map(new KeyValueMapper<String, GMTTEventOnlyIDs, KeyValue<String, GMTTEventOnlyIDs>>() {
                    @Override
                    public KeyValue<String, GMTTEventOnlyIDs> apply(String s, GMTTEventOnlyIDs gmttEvent) {
                        logger.info("Step 6: To be enriched event found. LocalComponentId:"+ gmttEvent.getLocalComponentId() + " EventId:"+ gmttEvent.getEventId());

                        return new KeyValue<>(gmttEvent.getLocalComponentId(), gmttEvent);
                    }
                })
                // Write to topic
                // NOTE: only by explicitly writing to a topic the Serdes can be enforced (or so it seems)
                .through(stringSerde, gmtteventOnlyIDsSerde, "gmtt-events-by-local")
                ;

        // Step 7: Join events without componentId with table with componentIds using the localComponentId (this is 1 of 4 possible joins)
        KStream<String, String> joinedLocalLocal = eventByLocalKStream
                .join(ComponentIDbyLocalID, new ValueJoiner<GMTTEventOnlyIDs, String, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(GMTTEventOnlyIDs gmttEvent, String componentID) {
                        logger.info("STep 7: Enrichment found LocalLocal: " + gmttEvent.getEventId() + "]*[" + componentID);
                        // The outcome of join is a value that consists of a keyvalue pair that will be mapped to the key and the value of the output stream in the next step
                        return new KeyValue<>(gmttEvent.getEventId(), componentID);
                    }
                })
                .map(new KeyValueMapper<String, KeyValue<String, String>, KeyValue<String, String>>() {
                    @Override
                    // The keyvalue pair that is the joined value of the above join is taken apart in key and value of the output stream
                    public KeyValue<String, String> apply(String k, KeyValue<String, String> eventIdComponentId) {
                        return new KeyValue<>(eventIdComponentId.key, eventIdComponentId.value);
                    }
                })
                ;
        joinedLocalLocal.to(stringSerde, stringSerde,"gmtt-enriched-events");

        return new KafkaStreams(builder, streamsConfiguration);
    }

}
