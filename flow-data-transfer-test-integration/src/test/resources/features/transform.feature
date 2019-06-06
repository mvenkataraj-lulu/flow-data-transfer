@Transform
Feature: Transform job
  Service will execute sql from file passed as a sqlPath in the request body.
  File should already exist withing project as resource

  @Sanity
  Scenario: Negative path - sql file does not exist
    When Transform request is sent to batch endpoint with body
    """
    {
      "sqlPath":"noSuchFile.sql"
    }
    """
    Then Such response is expected
    """
    {
      "jobId": "#IGNORE#",
      "executionId": "#IGNORE#",
      "code": "UNKNOWN",
      "status": "START",
      "message": ""
    }
    """
    When Batch request completed with response
    """
    {
      "jobId": "#IGNORE#",
      "executionId": "#IGNORE#",
      "code": "FAILED",
      "status": "FAILED",
      "message": "#IGNORE#"
    }
    """

  @Sanity
  Scenario Outline: Validate RP team transform scenarios
    When Transform request is sent to batch endpoint with body
    """
    {
      "sqlPath":"<Path>"
    }
    """
    Then Such response is expected
    """
    {
      "jobId": "#IGNORE#",
      "executionId": "#IGNORE#",
      "code": "UNKNOWN",
      "status": "START",
      "message": ""
    }
    """
    When Batch request completed with response
    """
    {
      "jobId": "#IGNORE#",
      "executionId": "#IGNORE#",
      "code": "COMPLETED",
      "status": "COMPLETED",
      "message": ""
    }
    """
    Examples:
      | Path                                                           |
      | sql/transform/product-transform.sql                            |