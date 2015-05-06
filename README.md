### Setup the database
Make sure Elasticsearch is started.

    sbt console
    mogopay.handlers.DBInitializationHandler.boot(true, true)
    exit

This will initialize the database and import the fixtures.

### Start the server

    sbt
    re-start

### Production mode

    export MOGOPAY_PROD="true"

### Jobs
#### Configuration
Set the initial delay and interval for each job in the `jobs.cron.` section of *application.conf*.

#### Launching ImportCountriesJob

    sbt
    run-main mogopay.jobs.RunImportCountriesJob

#### Starting a job manually
Start the job *XXX* by passing an `ActorSystem` to `mogopay.jobs.XXXJob.start`.
