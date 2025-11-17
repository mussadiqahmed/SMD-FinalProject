function toStoredList(value) {
  if (!value) return JSON.stringify([]);
  if (Array.isArray(value)) return JSON.stringify(value);
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      if (Array.isArray(parsed)) return JSON.stringify(parsed);
    } catch (error) {
      const csv = value.split(',').map((item) => item.trim()).filter(Boolean);
      return JSON.stringify(csv);
    }
  }
  return JSON.stringify([]);
}

function fromStoredList(value) {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
}

module.exports = {
  toStoredList,
  fromStoredList
};

