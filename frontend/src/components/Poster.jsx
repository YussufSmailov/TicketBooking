import { useState } from "react";

const gradients = [
  ["#d6403a", "#5b1d6b"],
  ["#1f9d55", "#0d3b4f"],
  ["#8a5cf0", "#1d2a6b"],
  ["#e8b04a", "#9b2c2c"],
  ["#2f80ed", "#123a5c"],
  ["#e9803a", "#3a1c4f"]
];

function pickGradient(title) {
  let sum = 0;
  for (let i = 0; i < title.length; i++) {
    sum = sum + title.charCodeAt(i);
  }
  const colors = gradients[sum % gradients.length];
  return `linear-gradient(160deg, ${colors[0]}, ${colors[1]})`;
}

function Poster({ url, title, className }) {
  const [failed, setFailed] = useState(false);

  if (!url || failed) {
    return (
      <div className={`poster poster-fallback ${className || ""}`} style={{ background: pickGradient(title) }} role="img" aria-label={title}>
        <span>{title}</span>
      </div>
    );
  }

  return (
    <img
      className={`poster ${className || ""}`}
      src={url}
      alt={title}
      loading="lazy"
      onError={() => setFailed(true)}
    />
  );
}

export default Poster;
