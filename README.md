### Setup the database
Make sure Elasticsearch is started.

    sbt
    console
    mogopay.handlers.BootHandler.boot(true, true)
    exit
    exit

This will initialize the database and import the fixtures.

The `exit`s are important because a bug eats all CPU when the import is run.

### Start the server

    re-start

### Production mode

    export MOGOPAY_PROD="true"

### Jobs
Specify the CRON expression in *src/main/resources/application.conf*, `akka.quartz.schedules`.

Start the jobs by passing an `ActorSystem` to `mogopay.jobs.*Job.start`.
