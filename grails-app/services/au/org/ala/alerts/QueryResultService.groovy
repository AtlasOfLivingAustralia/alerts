package au.org.ala.alerts

class QueryResultService {

    def get(id){
        QueryResult qs = QueryResult.get(id)
        if (qs) {
            Query query = Query.get(qs.query.id)
            qs.query = query

            Frequency frequency = Frequency.get(qs.frequency.id)
            qs.frequency = frequency
        }
        return qs
    }
}
