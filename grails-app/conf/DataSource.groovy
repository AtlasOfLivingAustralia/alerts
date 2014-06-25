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
            username = "root"
            password = "password"
            driverClassName = "com.mysql.jdbc.Driver"
        }
    }

    production {
        dataSource {
            dbCreate = "update"
        }
    }
}