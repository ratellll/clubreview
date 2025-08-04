import React from 'react';

const Alert = ({ type = 'info', message, onClose, dismissible = false }) => {
    if (!message) return null;

    return (
        <div className={`alert alert-${type} ${dismissible ? 'alert-dismissible' : ''} fade show`} role="alert">
            {message}
            {(onClose || dismissible) && (
                <button
                    type="button"
                    className="btn-close"
                    onClick={onClose}
                    aria-label="Close"
                ></button>
            )}
        </div>
    );
};

export default Alert;