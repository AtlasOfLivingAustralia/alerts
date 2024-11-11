<div name="subscribers" id="subscribers_${queryid}">

    <g:each in="${subscribers}" var="subscriber">
         %{-- subscriber.id is the sequence id , not the ALA user id    --}%
        <span class="badge badge-info">${subscriber.email} <i onclick="unsubscribe(${queryid}, ${subscriber.id},'${subscriber.email}')" class="fas fa-trash clickable"  ></i></span>
    </g:each>

    <g:if test="${subscribers.size() == 0}">
        <div>
        <button name="deleteSubscription_${queryid}" class="btn btn-primary" onclick="deleteSubscription(${queryid})"> Delete this subscription</button>
        </div>
    </g:if>
</div>