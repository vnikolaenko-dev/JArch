import React, { useState, useEffect, useRef } from 'react';
import AppConfigEditor from '../components/AppConfigEditor';
import EntityConfigEditor from '../components/EntityConfigEditor';

const AppConfigBuilderPage = ({ 
    onAppConfigChange, 
    onEntityConfigChange,
    onAppConfigValidationChange,
    onEntityConfigValidationChange,
    onEditorTabChange,
    initialAppConfig = null,
    initialEntityConfig = null
}) => {
    const [activeTab, setActiveTab] = useState(() => {
        const savedTab = localStorage.getItem('configBuilderActiveTab');
        return savedTab || 'app';
    });
    
    const [appConfig, setAppConfig] = useState(initialAppConfig);
    const [entityConfig, setEntityConfig] = useState(initialEntityConfig);
    
    const [appEditorKey, setAppEditorKey] = useState(Date.now());
    const [entityEditorKey, setEntityEditorKey] = useState(Date.now());
    
    const appEditorRef = useRef(null);
    const entityEditorRef = useRef(null);

    useEffect(() => {
        localStorage.setItem('configBuilderActiveTab', activeTab);
        if (onEditorTabChange) {
            onEditorTabChange(activeTab);
        }
    }, [activeTab, onEditorTabChange]);

    useEffect(() => {
        if (initialAppConfig) {
            setAppConfig(initialAppConfig);
            setAppEditorKey(Date.now());
        }
    }, [initialAppConfig]);

    useEffect(() => {
        if (initialEntityConfig) {
            setEntityConfig(initialEntityConfig);
            setEntityEditorKey(Date.now());
        }
    }, [initialEntityConfig]);

    const handleAppConfigChange = (newConfig) => {
        setAppConfig(newConfig);
        if (onAppConfigChange) {
            onAppConfigChange(newConfig);
        }
    };

    const handleEntityConfigChange = (newConfig) => {
        setEntityConfig(newConfig);
        if (onEntityConfigChange) {
            onEntityConfigChange(newConfig);
        }
    };

    const handleAppConfigValidation = (isValid, errors = []) => {
        if (onAppConfigValidationChange) {
            onAppConfigValidationChange(isValid, errors);
        }
    };

    const handleEntityConfigValidation = (isValid, errors = []) => {
        if (onEntityConfigValidationChange) {
            onEntityConfigValidationChange(isValid, errors);
        }
    };

    const handleTabChange = (tab) => {
        setActiveTab(tab);
    };

    return (
        <div className="config-builder-page">
            <div className="config-builder-controls">
                <button 
                    type="button"
                    onClick={() => handleTabChange('app')}
                    className={activeTab === 'app' ? 'active' : ''}
                >
                    [app-config.json]
                </button>
                <button 
                    type="button"
                    onClick={() => handleTabChange('entity')}
                    className={activeTab === 'entity' ? 'active' : ''}
                >
                    [entity-config.json]
                </button>
            </div>

            <div className="json-editor-container">
                {activeTab === 'app' ? (
                    <div className="json-editor-wrapper" ref={appEditorRef}>
                        <AppConfigEditor 
                            key={`app-${appEditorKey}`}
                            onChange={handleAppConfigChange}
                            onValidationChange={handleAppConfigValidation}
                            initialData={appConfig}
                        />
                    </div>
                ) : (
                    <div className="json-editor-wrapper" ref={entityEditorRef}>
                        <EntityConfigEditor 
                            key={`entity-${entityEditorKey}`}
                            onChange={handleEntityConfigChange}
                            onValidationChange={handleEntityConfigValidation}
                            initialData={entityConfig}
                        />
                    </div>
                )}
            </div>
        </div>
    );
};

export default AppConfigBuilderPage;