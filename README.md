# EventsCorrelation
Kafka Streams solution to correlate events on alternative identifiers

Use case:
Applications that process transactions ('components') send status events to a certain event collection facility. The stream contains events that may have a proper unique identifier ('componentID') of the component it refers to, or it may not have the unique identifier, but alternative identifers such as a identifier an application assigns locally (localComponentID). Also, events my contain an identifer that another application has assigned ('externalComponentID'). Finally, events may contain both a componentId and a localComponentID and/or an externalComponentID. These latter events provide information to correlate events with out componentID with those that have.
