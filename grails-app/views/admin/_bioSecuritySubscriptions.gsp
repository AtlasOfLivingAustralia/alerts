    <g:each status="i" in="${queries}" var="query">
        <g:render template="bioSecuritySubscription" model="[i: i, query: query,  subscribers: subscribers[i], logs: logs[i], startIdx: startIdx]"/>
    </g:each>
