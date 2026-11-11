import { useEffect, useState } from "react";

export function useData(loadFunction, dependencies) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadCount, setReloadCount] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    loadFunction()
      .then((result) => {
        if (!cancelled) {
          setData(result);
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e.message);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [...dependencies, reloadCount]);

  function reload() {
    setReloadCount((count) => count + 1);
  }

  return { data: data, loading: loading, error: error, reload: reload };
}
