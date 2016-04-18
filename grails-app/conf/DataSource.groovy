dataSource {
    pooled = true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/alerts"
            username = "alerts_user"
            password = "alerts_user"
            driverClassName = "com.mysql.jdbc.Driver"
        }
    }
    test {
        dataSource {
            dialect = "org.hibernate.dialect.H2Dialect"
            dbCreate = "create-drop"
            url = "jdbc:h2:mem:alerts;MVCC=TRUE;LOCK_TIMEOUT=10000;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE;"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
        }
    }
}