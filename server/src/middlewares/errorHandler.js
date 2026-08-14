export function errorHandler(err, req, res, next) {
  console.error('[ServerError]', err);
  const status = err.status || 500;
  res.status(status).json({
    error: {
      message: err.message || 'Internal Server Error',
      status
    }
  });
}
