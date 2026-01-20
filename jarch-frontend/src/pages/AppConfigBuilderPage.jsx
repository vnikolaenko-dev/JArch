import React, { useState, useEffect } from 'react';
import AppConfigEditor from '../components/AppConfigEditor';
import EntityConfigEditor from '../components/EntityConfigEditor';

const AppConfigBuilderPage = ({ 
    onAppConfigChange, 
    onEntityConfigChange,
    onAppConfigValidationChange,
    onEntityConfigValidationChange,
    initialAppConfig = null,
    initialEntityConfig = null
}) => {
    const [activeTab, setActiveTab] = useState('app');
    const [appConfig, setAppConfig] = useState(initialAppConfig);
    const [entityConfig, setEntityConfig] = useState(initialEntityConfig);
    
    const [appEditorKey, setAppEditorKey] = useState(Date.now());
    const [entityEditorKey, setEntityEditorKey] = useState(Date.now());

    useEffect(() => {
        console.log('AppConfigBuilderPage получил новые initialAppConfig:', initialAppConfig);
        if (initialAppConfig) {
            setAppConfig(initialAppConfig);
            setAppEditorKey(Date.now());
        }
    }, [initialAppConfig]);

    useEffect(() => {
        console.log('AppConfigBuilderPage получил новые initialEntityConfig:', initialEntityConfig);
        if (initialEntityConfig) {
            setEntityConfig(initialEntityConfig);
            setEntityEditorKey(Date.now());
        }
    }, [initialEntityConfig]);

    const handleAppConfigChange = (newConfig) => {
        console.log('App config изменился в AppConfigBuilderPage:', newConfig);
        setAppConfig(newConfig);
        if (onAppConfigChange) {
            onAppConfigChange(newConfig);
        }
    };

    const handleEntityConfigChange = (newConfig) => {
        console.log('Entity config изменился в AppConfigBuilderPage:', newConfig);
        setEntityConfig(newConfig);
        if (onEntityConfigChange) {
            onEntityConfigChange(newConfig);
        }
    };

    const handleAppConfigValidation = (isValid) => {
        if (onAppConfigValidationChange) {
            onAppConfigValidationChange(isValid);
        }
    };

    const handleEntityConfigValidation = (isValid) => {
        if (onEntityConfigValidationChange) {
            onEntityConfigValidationChange(isValid);
        }
    };

    return (
        <div className="config-builder-page">
            <div className="config-builder-controls">
                <button 
                    onClick={() => setActiveTab('app')}
                    className={activeTab === 'app' ? 'active' : ''}
                >
                    [app-config.json]
                </button>
                <button 
                    onClick={() => setActiveTab('entity')}
                    className={activeTab === 'entity' ? 'active' : ''}
                >
                    [entity-config.json]
                </button>
            </div>

            <div className="json-editor-container">
                {activeTab === 'app' ? (
                    <div className="json-editor-wrapper">
                        <AppConfigEditor 
                            key={`app-${appEditorKey}`}
                            onChange={handleAppConfigChange}
                            onValidationChange={handleAppConfigValidation}
                            initialData={appConfig}
                        />
                    </div>
                ) : (
                    <div className="json-editor-wrapper">
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