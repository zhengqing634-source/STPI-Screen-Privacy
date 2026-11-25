#!/system/bin/sh
# STPI Screen Privacy - Android 14+ diagnostic helper
# Read-only: does not change AppOps, permissions, system properties, or other apps.
# Usage:
#   su -c sh /sdcard/STPI_Screen_Privacy.sh check
#   su -c sh /sdcard/STPI_Screen_Privacy.sh monitor 5
#   su -c sh /sdcard/STPI_Screen_Privacy.sh help

PKG="com.chaoxing.mobile"
OUTDIR="/sdcard/STPI_Screen_Privacy"
LOG="$OUTDIR/privacy_monitor.log"
INTERVAL="${2:-5}"

mkdir -p "$OUTDIR" 2>/dev/null

section() { echo; echo "===== $1 ====="; }

projection() {
  dumpsys media_projection 2>&1
}

virtual_display_instances() {
  # Only report actual virtual display device records, not the mere presence
  # of the VirtualDisplayAdapter class name.
  dumpsys display 2>&1 | grep -E 'VirtualDisplayDevice|type VIRTUAL|uniqueId="virtual:' || true
}

appops() {
  cmd appops get "$PKG" PROJECT_MEDIA 2>&1
}

check() {
  echo "STPI Screen Privacy"
  echo "Time: $(date 2>/dev/null)"
  echo "Android: $(getprop ro.build.version.release) (API $(getprop ro.build.version.sdk))"
  echo "Device: $(getprop ro.product.model)"
  echo "Target package: $PKG"

  section "MEDIA PROJECTION"
  projection

  section "VIRTUAL DISPLAY INSTANCES"
  VDISP="$(virtual_display_instances)"
  if [ -n "$VDISP" ]; then
    echo "$VDISP"
  else
    echo "No matching active virtual-display device record found in dumpsys display."
  fi

  section "APP OPERATION"
  appops

  section "PRIVACY INTERPRETATION"
  echo "- MediaProjection 'null' means this service reports no active session at query time."
  echo "- PROJECT_MEDIA AppOps is supplementary evidence, not a complete capture detector."
  echo "- VirtualDisplayAdapter by itself is a framework component, not proof of an active virtual display."
  echo "- OEM/private capture paths may not be visible to this script."
  echo "- This script does not block or stop another app's screen capture."
  echo
  echo "Log file: $LOG"
}

monitor() {
  case "$INTERVAL" in
    ''|*[!0-9]*) INTERVAL=5 ;;
  esac
  [ "$INTERVAL" -lt 2 ] && INTERVAL=2
  echo "Monitoring every ${INTERVAL}s. Press Ctrl+C to stop."
  echo "Read-only monitor; no permissions or services will be changed."
  while :; do
    {
      echo
      echo "===== $(date 2>/dev/null) ====="
      echo "[media_projection]"
      projection
      echo "[virtual_display_instances]"
      virtual_display_instances
      echo "[appops]"
      appops
    } >> "$LOG" 2>&1
    clear 2>/dev/null
    echo "STPI Screen Privacy monitor | $(date 2>/dev/null)"
    echo "Log: $LOG"
    echo
    projection | tail -n 12
    echo
    echo "Virtual display records:"
    virtual_display_instances
    echo
    echo "AppOps:"
    appops
    echo
    echo "Reminder: absence of visible records is not a guarantee against OEM capture paths."
    sleep "$INTERVAL"
  done
}

case "${1:-check}" in
  check) check ;;
  monitor) monitor ;;
  help|-h|--help)
    echo "Usage:"
    echo "  su -c sh /sdcard/STPI_Screen_Privacy.sh check"
    echo "  su -c sh /sdcard/STPI_Screen_Privacy.sh monitor [seconds]"
    ;;
  *)
    echo "Unknown command: $1"
    echo "Run: sh $0 help"
    exit 2
    ;;
esac
