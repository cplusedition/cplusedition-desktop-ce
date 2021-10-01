rootdir=ROOT
pidfile="${rootdir}/run/server.pid"
if [ -e "$pidfile" ]; then
  cat "$pidfile" | xargs kill
  rm "$pidfile"
fi
