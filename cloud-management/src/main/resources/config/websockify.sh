#!/bin/bash
#
# Comments to support chkconfig
# chkconfig: - 98 02
# description: 服务名称 service script
#
# Source function library.
. /etc/init.d/functions

### Default variables
prog_name="websockify"
prog_path="/usr/bin/python3"
pidfile="/var/run/websockify.pid"
options="-m websockify --token-plugin TokenFile --token-source /usr/local/websockify/token/ 8080"

# Check if requirements are met
[ -x "/usr/local/websockify/scripts/" ] || exit 1

RETVAL=0

start(){
  echo -n $"Starting websockify: "
  daemon $prog_path $options
  RETVAL=$?
  PID=$(pidof ${prog_path})

  [ ! -z "${PID}" ] && echo ${PID} > ${pidfile}
  echo
  [ $RETVAL -eq 0 ] && touch /var/lock/subsys/$prog_name
  return $RETVAL
}

stop(){
  echo -n $"Shutting down $prog_name: "
  killproc -p ${pidfile}
  RETVAL=$?
  echo
  [ $RETVAL -eq 0 ] && rm -f /var/lock/subsys/$prog_name
  return $RETVAL
}

restart() {
  stop
  start
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    restart
    ;;
  status)
    status $prog_path
    RETVAL=$?
    ;;
  *)
    echo $"Usage: $0 {start|stop|restart|status}"
    RETVAL=1
esac

exit $RETVAL