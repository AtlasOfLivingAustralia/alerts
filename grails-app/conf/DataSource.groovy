dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    username = "root"
    password = "password"
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
            //dbCreate = "create-drop" // one of 'create', 'create-drop','update'
            dbCreate = "none"
            url = "jdbc:mysql://localhost:3306/postie"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/postie-test"
        }
    }
    production {
        dataSource {
            dbCreate = "none"
            url = "jdbc:mysql://ala-authdb1.vm.csiro.au:3306/postie"
            username = "postie"
            password ="RHitoHYAbfmJBCT0zywo143tH"
        }
    }
}