export function SkeletonLines({ count }) {
  const lines = [];
  for (let i = 0; i < count; i++) {
    lines.push(<div key={i} className="skeleton skeleton-line" style={{ width: `${90 - i * 12}%` }}></div>);
  }
  return <div aria-hidden="true">{lines}</div>;
}

export function SkeletonCards({ count }) {
  const cards = [];
  for (let i = 0; i < count; i++) {
    cards.push(
      <li key={i} className="skeleton-card">
        <div className="skeleton skeleton-poster"></div>
        <div className="skeleton-card-body">
          <div className="skeleton skeleton-line"></div>
          <div className="skeleton skeleton-line" style={{ width: "60%" }}></div>
        </div>
      </li>
    );
  }
  return <ul className="card-grid" aria-hidden="true">{cards}</ul>;
}

export function SkeletonPage() {
  return (
    <div aria-hidden="true">
      <div className="skeleton skeleton-title"></div>
      <SkeletonLines count={3} />
      <div className="skeleton skeleton-block"></div>
    </div>
  );
}
