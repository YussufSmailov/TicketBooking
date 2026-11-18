import { useState } from "react";

function Crest({ url, name }) {
  const [failed, setFailed] = useState(false);
  if (!url || failed) {
    return <span className="crest crest-fallback" aria-hidden="true">{name.slice(0, 1)}</span>;
  }
  return <img className="crest" src={url} alt="" loading="lazy" onError={() => setFailed(true)} />;
}

function MatchPoster({ homeName, awayName, homeLogoUrl, awayLogoUrl, backgroundUrl, className }) {
  const style = backgroundUrl ? { backgroundImage: `url("${backgroundUrl}")` } : undefined;
  return (
    <div className={`poster match-poster ${className || ""}`} style={style} role="img" aria-label={`${homeName} против ${awayName}`}>
      <div className="match-poster-teams">
        <div className="match-poster-team">
          <Crest url={homeLogoUrl} name={homeName} />
          <span>{homeName}</span>
        </div>
        <span className="match-poster-vs" aria-hidden="true">VS</span>
        <div className="match-poster-team">
          <Crest url={awayLogoUrl} name={awayName} />
          <span>{awayName}</span>
        </div>
      </div>
    </div>
  );
}

export default MatchPoster;
