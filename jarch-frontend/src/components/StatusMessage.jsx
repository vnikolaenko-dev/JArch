import React from 'react';

const StatusMessage = ({ error, success }) => {
    if (!error && !success) return null;

    return (
        <>
            {error && (
                <div className="error-message">
                    ⚠️ {error}
                </div>
            )}
            {success && (
                <div className="success-message">
                    ✅ {success}
                </div>
            )}
        </>
    );
};

export default StatusMessage;