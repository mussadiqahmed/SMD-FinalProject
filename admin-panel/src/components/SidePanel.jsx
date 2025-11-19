import './SidePanel.css';

export default function SidePanel({ open, title, onClose, children, width = 520 }) {
  if (!open) return null;

  return (
    <div className="sidepanel-overlay" role="presentation">
      <div className="sidepanel" style={{ width }}>
        <header className="sidepanel__header">
          <h3>{title}</h3>
          <button type="button" aria-label="Close" onClick={onClose}>
            &times;
          </button>
        </header>
        <div className="sidepanel__body">{children}</div>
      </div>
    </div>
  );
}
import './SidePanel.css';

export default function SidePanel({ open, title, onClose, children, width = 520 }) {
  if (!open) {
    return null;
  }

  return (
    <div className="sidepanel-overlay" role="presentation">
      <div className="sidepanel" style={{ width }}>
        <div className="sidepanel__header">
          <h3>{title}</h3>
          <button type se?? 

