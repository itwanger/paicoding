/**
 * 将时间戳格式化为年月日格式
 * @param {number|string} timestamp - 时间戳
 * @returns {string} 格式化后的日期字符串，格式：YYYY-MM-DD
 */
function formatDate(timestamp) {
  if (!timestamp) {
    return '';
  }
  const date = new Date(Number(timestamp));
  console.log(timestamp,date);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * 将时间戳格式化为年月日时分秒格式
 * @param {number|string} timestamp - 时间戳
 * @returns {string} 格式化后的日期时间字符串，格式：YYYY-MM-DD HH:mm:ss
 */
function formatDateTime(timestamp) {
  if (!timestamp) {
    return '';
  }
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

module.exports = {
  formatDate,
  formatDateTime
};
