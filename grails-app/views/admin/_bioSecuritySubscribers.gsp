<div name="subscribers" id="subscribers_${queryid}">
    <g:each in="${subscribers}" var="subscriber">
        <span class="badge badge-info" onclick="unsubscribe(${queryid}, '${subscriber.id}','${subscriber.email}')">${subscriber.email} <i class="fas fa-trash" ></i></span>
    </g:each>

    <g:if test="${subscribers.size() == 0}">
        <div>
        <button name="deleteSubscription_${queryid}" class="btn btn-primary" onclick="deleteSubscription(${queryid})"> Delete this subscription</button>
        </div>
    </g:if>
</div>