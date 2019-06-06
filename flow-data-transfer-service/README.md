# batch-service

Implemented via Spring Batch, one of the Spring Boot frameworks, this service
implements a REST API for triggering Batch Jobs.

It requires a hirer level coordination service, such as Control-M, Quartz, etc.
to initiate batch jobs at the right time, and in the right order.

It is intended to run as a Docker Container under EKS.