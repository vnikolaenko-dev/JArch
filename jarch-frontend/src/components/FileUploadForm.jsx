import React from 'react';

const FileUploadForm = ({
    entityFile,
    appFile,
    isGenerating,
    onEntityFileChange,
    onAppFileChange,
    onSubmit
}) => {
    return (
        <form onSubmit={onSubmit}>
            <div className="file-inputs">
                <div className="file-input">
                    <div className="file-label">
                        Конфигурация сущностей (entity-config.json):
                    </div>
                    <input 
                        type="file" 
                        accept=".json" 
                        onChange={onEntityFileChange}
                        required 
                        disabled={isGenerating}
                    />
                    {entityFile && (
                        <div className="file-selected">
                            ✓ {entityFile.name}
                        </div>
                    )}
                </div>
                <div className="file-input">
                    <div className="file-label">
                        Конфигурация приложения (app-config.json):
                    </div>
                    <input 
                        type="file" 
                        accept=".json" 
                        onChange={onAppFileChange}
                        required 
                        disabled={isGenerating}
                    />
                    {appFile && (
                        <div className="file-selected">
                            ✓ {appFile.name}
                        </div>
                    )}
                </div>
            </div>
        </form>
    );
};

export default FileUploadForm;