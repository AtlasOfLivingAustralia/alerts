

def dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * 3600)
println(dateToUse)

dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * 86400)
println(dateToUse)

dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * 604800)
println(dateToUse)

dateToUse = org.apache.commons.lang.time.DateUtils.addSeconds(new Date(), -1 * 2419200)
println(dateToUse)