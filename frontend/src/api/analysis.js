export function streamAnalysis(files, projectName) {
  const controller = new AbortController()

  const promise = fetch('/api/v1/analysis/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ files, projectName }),
    signal: controller.signal
  })

  return { controller, response: promise }
}
