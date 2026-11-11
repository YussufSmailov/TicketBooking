import { SkeletonCards, SkeletonLines, SkeletonPage } from "./Skeleton";

function StatusMessage({ loading, error, empty, emptyText, onRetry, skeleton }) {
  if (loading) {
    return (
      <div role="status" aria-label="Загрузка">
        {skeleton === "cards" && <SkeletonCards count={8} />}
        {skeleton === "page" && <SkeletonPage />}
        {skeleton !== "cards" && skeleton !== "page" && <SkeletonLines count={3} />}
      </div>
    );
  }
  if (error) {
    return (
      <div className="state-box" role="alert">
        <span className="state-icon" aria-hidden="true">⚠</span>
        <p className="error">{error}</p>
        {onRetry && <button className="secondary-button" onClick={onRetry}>Повторить</button>}
      </div>
    );
  }
  if (empty) {
    return (
      <div className="state-box">
        <span className="state-icon" aria-hidden="true">🎫</span>
        <p className="muted">{emptyText}</p>
      </div>
    );
  }
  return null;
}

export default StatusMessage;
