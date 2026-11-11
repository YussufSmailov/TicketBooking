import { useEffect, useState } from "react";

function Countdown({ seconds, onFinish }) {
  const [left, setLeft] = useState(seconds);

  useEffect(() => {
    if (left <= 0) {
      onFinish();
      return;
    }
    const timer = setTimeout(() => setLeft(left - 1), 1000);
    return () => clearTimeout(timer);
  }, [left]);

  const minutes = Math.floor(left / 60);
  const rest = String(left % 60).padStart(2, "0");
  return <span className="countdown">{minutes}:{rest}</span>;
}

export default Countdown;
