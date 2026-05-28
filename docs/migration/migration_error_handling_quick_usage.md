## Migration Error Handling — Quick Usage
Use `MigrationErrorPolicy` to control what happens when a migration helper encounters an error. 
``` groovy
import com.netgrif.application.engine.migration.model.MigrationErrorPolicy
```

### Available policies
``` groovy
MigrationErrorPolicy.continueOnError()        // log/cache errors and continue
MigrationErrorPolicy.throwImmediately()       // stop on first error
MigrationErrorPolicy.throwAfterLimit(10)      // stop after 10 errors
MigrationErrorPolicy.throwAfterProcessing()   // process all, then fail if errors exist
```

#### Basic usage
``` groovy
migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterProcessing()) {
    migrationHelper.updateAllCasesCursor({ Case useCase ->
        // migration logic
    })
}
```

#### Continue migration and inspect errors
``` groovy
migrationHelper.clearErrors()

migrationHelper.withErrorPolicy(MigrationErrorPolicy.continueOnError()) {
    migrationHelper.updateAllCasesCursor({ Case useCase ->
        // migration logic
    })
}

def errors = migrationHelper.popErrors()

errors.each { error ->
    log.warn("${error.entityType} ${error.entityId}: ${error.message}", error.cause)
}
```


#### Stop after a number of errors
``` groovy
migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterLimit(20)) {
    migrationHelper.updateAllTasksCursor({ Task task ->
        migrationHelper.elasticTaskIndex(task)
    })
}
```

 
#### Fail after full processing
``` groovy
import com.netgrif.application.engine.migration.throwable.MigrationErrorException

try {
    migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterProcessing()) {
        migrationHelper.updateAllCasesCursor({ Case useCase ->
            // migration logic
        })
    }
} catch (MigrationErrorException e) {
    log.error("Migration failed with ${e.errors.size()} errors")

    e.errors.each { error ->
        log.error("${error.entityType} ${error.entityId}: ${error.message}", error.cause)
    }

    throw e
}
```


#### Error cache helpers
``` groovy
migrationHelper.clearErrors()  // clears cached errors
migrationHelper.hasErrors()    // true if errors exist
migrationHelper.getErrors()    // returns errors without clearing
migrationHelper.popErrors()    // returns errors and clears cache
```

 
#### Recommended production pattern
``` groovy
migrationHelper.clearErrors()

migrationHelper.withErrorPolicy(MigrationErrorPolicy.throwAfterProcessing()) {
    // migration logic
}
```

Use throwAfterProcessing() for most production migrations because it collects a full error report and fails only after all possible records are processed.