import React from 'react';

const ErrorDisplay = ({ 
    activeTab = 'app',
    appErrors = [], 
    entityErrors = [] 
}) => {
    const currentErrors = activeTab === 'app' ? appErrors : entityErrors;
    const currentConfigName = activeTab === 'app' ? 'app-config.json' : 'entity-config.json';
    
    if (currentErrors.length === 0) {
        return null; 
    }
    
    return (
        <div className="error-display">
            <h4>Ошибки в {currentConfigName}:</h4>
            <div className="error-list">
                {currentErrors.map((error, index) => (
                    <div key={index} className="error-item">
                        {error}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ErrorDisplay;