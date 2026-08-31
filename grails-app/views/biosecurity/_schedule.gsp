<g:set var="today" value="${new java.text.SimpleDateFormat('yyyy-MM-dd').format(new Date())}"/>
<div  class="row"  style="text-align: right">
    <div name="scheduleStatusInfo"  class="col-sm-8">
        <g:if test="${!jobStatus}">
            <span style="color: red; font-weight: bold;">
                No job is scheduled
            </span>
        </g:if>

        <g:elseif test="${jobStatus.state == 'NORMAL'}">
            <%
                def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
            %>
            <span style="color: green; font-weight: bold;">
                Next run will be on <alerts:ISODateTime date="${jobStatus.nextFireTime}" /></span>
        </g:elseif>

        <g:else>
            <span style="color: red; font-weight: bold;">
                Warning: Alerts are ${jobStatus.state}
            </span>
        </g:else>
    </div>
    <div  class="col-sm-4">
        <button type="button" id="showScheduleBtn" class="btn btn-outline-primary">Schedule Manager</button>
        <a class="btn btn-outline-primary" href="${createLink( namespace: 'biosecurity',
                controller: 'csv', action: 'list')}" target="_blank">CSV Reporting</a>
    </div>
</div>
<p></p>

<div class="card card-body" id="rescheduleBiosecurity" x-data="scheduleInfo()" style="display:none;">
    <div class="text-center"><h3>Alerts schedule manager</h3></div>
    <div class="row mt-10" >
        <div class="col-sm-12"><h4>Pause or resume now</h4></div>
        <div class="col-sm-12">Pause or resume alerts scheduling immediately. &nbsp;
        <g:link controller="schedule" action="pauseAlerts" namespace="biosecurity" class="btn btn-outline-primary" >
            Pause now
        </g:link> &nbsp;
        <g:link controller="schedule" action="resumeAlerts" namespace="biosecurity" class="btn btn-outline-primary" >
            Resume now
        </g:link>
        <g:link controller="schedule" action="runNow" namespace="biosecurity" class="btn btn-primary ms-2" onclick="return confirm('Are you sure you want to run the biosecurity alerts now?');">
            Run now
        </g:link>
        </div>
    </div>
    <div class="mt-20"></div>
    <g:form name="pauseResumeForm" controller="schedule" action="pauseResumeAlerts"  namespace="biosecurity" method="post" >
        <div><h4>Schedule a pause</h4></div>
        <div class="row" >
            <div class="col-sm-12" >Set a date range to pause alerts. Dates start at midnight in your local timezone (<span class="js-local-timezone-label"></span>). You can save one scheduled pause at a time.</div>
            <input type="hidden" name="localTimeZone" class="js-local-timezone" />
            <div class="col-sm-12 mt-20">
                Pause from <input type="date" name="pauseDate" value="${today}" />
                Resume on  <input type="date" name="resumeDate" value="${today}" />
                &nbsp;&nbsp;
                <button type="submit"  class="btn btn-primary">Save schedule</button>
                &nbsp;
                <g:link controller="schedule" action="cancelScheduledPauseResumeJob" namespace="biosecurity" class="btn btn-outline-primary" >
                    Cancel scheduled pause
                </g:link>
            </div>
            <div class="col-sm-12 mt-20 " >
                <!-- Both a pause and a resume are scheduled: the normal, complete window -->
                <div x-show="pauseWindowInfo.pause.length>0 && pauseWindowInfo.resume.length>0" class="alert alert-info align-items-start mb-0" role="alert" x-cloak>
                    <i class="fa fa-clock-o me-2 mt-1"></i>
                    <span>
                        Alerts are scheduled to pause on <strong x-text="formatLocalDateTime(pauseWindowInfo.pause)"></strong>
                        and resume on <strong x-text="formatLocalDateTime(pauseWindowInfo.resume)"></strong>.
                    </span>
                </div>
            </div>
        </div>
    </g:form>

    <div class="row mt-30">
        <div class="col-sm-12">
            <h4>Schedule a weekly biosecurity job</h4>
            <p>You can change the weekday and time this job runs. This updates the weekly schedule only.</p>
        </div>

        <g:form controller="schedule" action="updateWeeklySchedule" namespace="biosecurity" method="POST" class="d-flex flex-wrap align-items-center gap-3 ms-3">
            <input type="hidden" name="localTimeZone" class="js-local-timezone" />
            <div class="mt-10">
                <label for="weekday">Run on</label>
                <select id="weekday" name="weekday" class="form-control">
                    <option value="MONDAY">Monday</option>
                    <option value="TUESDAY">Tuesday</option>
                    <option value="WEDNESDAY">Wednesday</option>
                    <option value="THURSDAY">Thursday</option>
                    <option value="FRIDAY">Friday</option>
                    <option value="SATURDAY">Saturday</option>
                    <option value="SUNDAY">Sunday</option>
                </select>
            </div>

            <!-- Time selector -->
            <div class="mt-10">
                <label for="time">At time</label>
                <input type="time" id="time" name="time" class="form-control" value="11:00">
            </div>

            <!-- Save button -->
            <div class="mt-10">
                <label>&nbsp;</label>
                <button type="submit" id="saveWeeklyScheduleBtn" class="btn btn-primary form-control">
                    Update Weekly Schedule
                </button>
            </div>
        </g:form>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        // The server resolves the bare "yyyy-MM-dd" pause/resume dates to midnight in this zone,
        // so it must be sent with every schedule form. Falls back to UTC on the server if blank.
        var localTimeZone = (Intl.DateTimeFormat().resolvedOptions().timeZone) || 'UTC';
        $('.js-local-timezone').val(localTimeZone);
        $('.js-local-timezone-label').text(localTimeZone);

        $('#showScheduleBtn').click(function() {
            $('#rescheduleBiosecurity').toggle();
            $(this).toggleClass('active');
            if ($(this).hasClass('active')) {
                $(this).text('Hide Schedule Manager');
            } else {
                $(this).text('Schedule Manager');
            }
        });
    });

    function scheduleInfo() {
        return {
            nextRunDate: '',
            planedPauseDate:'',
            planedResumeDate:'',
            pauseWindowInfo: {pause: '', resume: ''},
            init() {
                this.fetchPauseWindowInfo();
            },
            fetchPauseWindowInfo() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'getAlertsPauseWindow')}",
                    type: 'GET',
                    success: (data) => {
                        this.pauseWindowInfo = {
                            pause: data.pause || '',
                            resume: data.resume || ''
                        };
                    }
                })
            },

            plannedPauseResumeDate() {
                const today = new Date().toISOString().split('T')[0];
                return {
                    pauseDate: '',
                    resumeDate: '',

                    init() {
                        this.pauseDate = this.pauseWindowInfo.pause || today;
                        this.resumeDate = this.pauseWindowInfo.resume || today;
                    }
                }
            },
            formatLocalDateTime(dateTime) {
                if (!dateTime) return '';
                return new Date(dateTime).toLocaleString();
            }
        }
    }
</script>