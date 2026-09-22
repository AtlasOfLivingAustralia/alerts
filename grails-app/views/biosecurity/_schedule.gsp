<div x-data="scheduleInfo()"  name="biosecurityScheduleInfo" class="container-fluid">
    <div class="row"  style="text-align: right" >
        <div name="statusInfo"  class="col-sm-8">
            <template x-if="jobStatus">
                <div>
                    <span  x-text="jobStatus.state === 'NORMAL'
                               ? 'Next run will be on ' + formatLocalDateTime(jobStatus.nextFireTime)
                               : 'Warning: Alerts are ' + jobStatus.state"

                            :style="jobStatus.state === 'NORMAL'
                                ? 'color: green; font-weight: bold;'
                                : 'color: red; font-weight: bold;'">
                    </span>
                </div>
            </template>
        </div>
        <div  class="col-sm-4">
            <button type="button" id="showScheduleBtn" class="btn btn-outline-primary"
                    @click="showSchedule = !showSchedule"
                    x-text="showSchedule ? 'Hide Schedule Manager' : 'Schedule Manager'">Schedule Manager</button>
            <a class="btn btn-outline-primary" href="${createLink( namespace: 'biosecurity',
                    controller: 'csv', action: 'list')}" target="_blank">CSV Reporting</a>
        </div>
    </div>
    <p></p>

    <div class="card card-body" id="rescheduleBiosecurity" x-show.important="showSchedule">
        <div class="text-center"><h4>Alerts schedule manager</h4></div>
        <div class="row mt-10" >
            <div class="col-sm-12"><h4>Pause or resume the schedule</h4></div>
            <div class="col-sm-12">Pause or resume schedules, or run Biosecurity alerts immediately. &nbsp;
            <button type="button" class="btn btn-outline-primary" @click="pause()">
                Pause now
            </button> &nbsp;
            <button type="button" class="btn btn-outline-primary" @click="resume()">
                Resume now
            </button>
            </div>
            <div class="col-sm-12">Run Biosecurity alerts immediately.
                <button  class="btn btn-primary ms-2" @click="confirm('Are you sure you want to run the biosecurity alerts now?') && runNow()">
                    Run now
                </button>
            </div>
        </div>
        <div name="pauseResumeForm" class="mt-20">
            <div><h4>Schedule a pause</h4></div>
            <div class="row" >
                <div class="col-sm-12" >Set a date range to pause alerts. Dates start at midnight in your local timezone. You can save one scheduled pause at a time.</div>
                <div class="col-sm-12 mt-20">
                    Pause from <input type="date" name="pauseDate"  x-model="planedPauseDate" />
                    Resume on  <input type="date" name="resumeDate"  x-model="planedResumeDate" />
                    &nbsp;&nbsp;
                    <button class="btn btn-primary" @click="pauseResumeAlerts()" >Save schedule</button>
                    &nbsp
                    <button  class="btn btn-outline-primary"  @click="cancelScheduledPauseResumeJob()">
                        Cancel scheduled pause
                    </button>
                </div>
                <div class="col-sm-12 mt-20 " >
                    <!-- Both a pause and a resume are scheduled: the normal, complete window -->
                    <div x-show.important="pauseWindowInfo.pause.length>0 && pauseWindowInfo.resume.length>0" class="alert alert-info align-items-start mb-0" role="alert" x-cloak>
                        <i class="fa fa-clock-o me-2 mt-1"></i>
                        <span>
                            Alerts are scheduled to pause on <strong x-text="formatLocalDateTime(pauseWindowInfo.pause)"></strong>
                            and resume on <strong x-text="formatLocalDateTime(pauseWindowInfo.resume)"></strong>.
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-30">
            <div class="col-sm-12">
                <h4>Schedule a weekly biosecurity job</h4>
                <p>You can change the weekday and time this job runs. This updates the weekly schedule only.</p>
            </div>

            <div class="d-flex flex-wrap align-items-center gap-3 ms-3">
                <div class="mt-10">
                    <label for="weekday">Run on</label>
                    <select id="weekday" name="weekday" class="form-control" x-model="cronWeekDay">
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
                    <input type="time" id="time" name="time" class="form-control" value="11:00" x-model="cronTime">
                </div>

                <!-- Save button -->
                <div class="mt-10">
                    <label>&nbsp;</label>
                    <button type="button" class="btn btn-primary form-control" @click="updateWeeklySchedule()">
                        Update Weekly Schedule
                    </button>
                </div>
            </div>
        </div>

        <div class="col-sm-12 mt-20">
            <h4>Monitoring team</h4>
            A completion summary email, including any successes or failures, will be sent to:
            <template x-for="member in monitoringMembers">
                <span class="badge-outline-primary me-2">
                    <span x-text="member?.email"></span>&nbsp;
                    <span class="fa fa-user-times cursor-pointer" @click="removeMonitoringTeamMember(member)"></span>
                </span>
            </template>
            &nbsp; <span class="fa fa-user-plus badge-outline-primary cursor-pointer" @click="addMonitoringTeamMember()"></span>
        </div>

    </div>
</div>

<script type="text/javascript">
    function scheduleInfo() {
        return {
            showSchedule: false,
            localTimeZone:(Intl.DateTimeFormat().resolvedOptions().timeZone) || 'UTC',
            cronWeekDay: "MONDAY",
            cronTime: "11:00",

            planedPauseDate:'',
            planedResumeDate:'',

            pauseWindowInfo: {pause: '', resume: ''},
            jobStatus: {},
            monitoringMembers: [],

            init() {
                const today = this.getLocalDate();
                this.planedPauseDate = today;
                this.planedResumeDate = today;
                this.fetchPauseWindowInfo();
                this.fetchJobStatus();
                this.getMonitoringMembers();
            },

            async getMonitoringMembers() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'admin', action: 'getMonitoringTeamMembers')}",
                    type: 'GET',
                    success: (data) => {
                        this.monitoringMembers = data;
                    }
                });
            },

            addMonitoringTeamMember() {
                const email = prompt("Enter the email address of the new monitoring team member:");
                if (email) {
                    //email validation
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(email)) {
                        alert('Invalid email address. Please enter a valid email.');
                        return;
                    }
                    $.ajax({
                        url: "${createLink(namespace: 'biosecurity', controller: 'admin', action: 'addMonitoringTeamMember')}",
                        type: 'POST',
                        data: { email: email },
                        success: (data) => {
                            if(data.success){
                                this.monitoringMembers.push(data.member);
                            } else {
                                alert('Error adding monitoring team member: ' + data.message);
                            }
                        },
                        error: (xhr, status, error) => {
                            alert('Error adding monitoring team member: ' + xhr.responseText);
                        }
                    });
                }
            },
            removeMonitoringTeamMember(member) {
                if (confirm('Are you sure you want to remove ' + member.email + ' from the monitoring team?')) {
                    $.ajax({
                        url: "${createLink(namespace: 'biosecurity', controller: 'admin', action: 'getMonitoringTeamMembers')}" + '/' + member.id,
                        type: 'DELETE',
                        success: (data) => {
                            if(data.success){
                                this.monitoringMembers = this.monitoringMembers.filter(m => m.id !== member.id);
                            } else {
                                alert('Error removing monitoring team member: ' + data.message);
                            }
                        },
                        error: (xhr, status, error) => {
                            alert('Error removing monitoring team member: ' + xhr.responseText);
                        }
                    });
                }
            },

            pause() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'pauseAlerts')}",
                    type: 'POST',
                    success: (data) => {
                        this.jobStatus = data;
                    },
                    error: (xhr, status, error) => {
                        alert('Error pausing alerts scheduling: ' + xhr.responseText);
                    }
                });
            },
            resume() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'resumeAlerts')}",
                    type: 'POST',
                    success: (data) => {
                        this.jobStatus = data;
                    },
                    error: (xhr, status, error) => {
                        alert('Error resuming alerts scheduling: ' + xhr.responseText);
                    }
                });
            },
            runNow() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'runNow')}",
                    type: 'POST',
                    success: (data) => {
                        if(data.success){
                            this.jobStatus.state = 'RUNNING';
                        }
                    },
                    error: (xhr, status, error) => {
                        alert('Error running alerts now: ' + xhr.responseText);
                    }
                });
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
                });
            },
            fetchJobStatus() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'getJobStatus')}",
                    type: 'GET',
                    success: (data) => {
                        this.jobStatus = data;
                        if (this.jobStatus.nextFireTime) {
                            this.cronWeekDay = this.getWeekDay(this.jobStatus.nextFireTime);
                            this.cronTime = this.getHourMinute(this.jobStatus.nextFireTime);
                        }
                    }
                });
            },

            updateWeeklySchedule() {
                const weekDay = this.cronWeekDay;
                const time = this.cronTime;
                const localTimeZone = this.localTimeZone;
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'updateWeeklySchedule')}",
                    type: 'POST',
                    data: {
                        localTimeZone: localTimeZone,
                        weekday: weekDay,
                        time: time
                    },
                    success: (data) => {
                        this.jobStatus = data;
                    }
                })
            },

            pauseResumeAlerts() {
                const pauseDate = this.planedPauseDate;
                const resumeDate = this.planedResumeDate;
                const localTimeZone = this.localTimeZone;
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'pauseResumeAlerts')}",
                    type: 'POST',
                    data: {
                        localTimeZone: localTimeZone,
                        pauseDate: pauseDate,
                        resumeDate: resumeDate
                    },
                    success: (data) => {
                        if (data.success) {
                            this.pauseWindowInfo.pause = data.window.pause;
                            this.pauseWindowInfo.resume = data.window.resume;
                        } else {
                            alert('Error scheduling alerts pause/resume: ' + data.message);
                        }
                    },
                    error: (xhr, status, error) => {
                        alert('Error pausing alerts scheduling: ' + xhr.responseText);
                    }
                })
            },

            cancelScheduledPauseResumeJob() {
                $.ajax({
                    url: "${createLink(namespace: 'biosecurity', controller: 'schedule', action: 'cancelScheduledPauseResumeJob')}",
                    type: 'POST',
                    success: (data) => {
                        if (data.success) {
                            this.pauseWindowInfo.pause = '';
                            this.pauseWindowInfo.resume = '';
                        } else {
                            alert('Error cancelling scheduled pause/resume job: ' + data.message);
                        }
                    }
                })
            },

            getWeekDay(date) {
                const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
                const d = new Date(date);
                return days[d.getDay()];
            },

            getHourMinute(date) {
                const d = new Date(date);
                const hours = d.getHours().toString().padStart(2, '0');
                const minutes = d.getMinutes().toString().padStart(2, '0');
                return hours + ':' + minutes;
            },

            formatLocalDateTime(dateTime) {
                if (!dateTime) return '';
                return new Date(dateTime).toLocaleString(undefined, {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: 'numeric',
                    minute: '2-digit'
                });
            },

            getLocalDate() {
                const parts = new Intl.DateTimeFormat(undefined, {
                    timeZone: this.localTimeZone,
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit'
                }).formatToParts(new Date());

                const values = Object.fromEntries(
                    parts.map(({ type, value }) => [type, value])
                );

                return values.year + '-' + values.month + '-' + values.day;
            }
        }
    }
</script>