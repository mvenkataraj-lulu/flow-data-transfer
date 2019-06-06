# flow-data-transfer

Git repository for flow-batch components

# flow-data-transfer-service

Batch Service for Scheduling and Triggering Recurring Jobs

## Transformation Job

This basically just executes specific SQL scripts/transactions. SQL is defined
as resources files on the java classpath, and the resource file names/paths are
specified in the REST API Requests.

## Transportation Job

See [flow-data](https://github.com/Lululemon/flow-data) for some working examples
of Anaplan Data Transport, as well as ODS access via Hibernate. See also 
[Flow Anaplan Endpoint](https://lululemon.atlassian.net/wiki/spaces/MPA/pages/819364321/Flow+Anaplan+Endpoint)
and
[Flow Anaplan Data Transport](https://lululemon.atlassian.net/wiki/spaces/MPA/pages/820871964/Flow+Anaplan+Data+Transport).

This basically just executes specific data transfers, where the details are
specified in the REST API Request.

### Upload Steps

1. Collect data from and ODS table.
1. Convert to a CSV file on batch-service file system.
1. Push CSV file to Amazon S3 for audit troubleshooting purposes
1. Using Anaplan Connect
   1. Upload CSV file to Anaplan Landing Area
   1. Import CSV file from Landing Area to model in Anaplan Data Hub
   1. Run appropriate processes on model for Anaplan use

### Download Steps

1. Using Anaplan Connect
   1. Run appropriate processes on model for exporting model
   1. Export model from Data Hub to CSV file in Landing Area
   1. Download CSV file from landing area to batch-service file system
1. Push CSV file to S3 for audit and troubleshooting
1. Load CSV file into ODS

## Seeding Job

This may not be needed, so this is just a placeholder.

