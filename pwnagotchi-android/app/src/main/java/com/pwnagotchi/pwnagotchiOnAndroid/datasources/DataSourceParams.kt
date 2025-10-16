package com.pwnagotchi.pwnagotchiOnAndroid.datasources

import java.net.URI

/**
 * A sealed class representing the different parameter types for starting a data source.
 * This provides a type-safe way to pass parameters to the `PwnagotchiDataSource.start` method.
 */
sealed class DataSourceParams {
    data class Remote(val uri: URI) : DataSourceParams()
    object Local : DataSourceParams()
}
