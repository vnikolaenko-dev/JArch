import React from 'react';

const GenerationControls = ({ areFilesReady, isGenerating, onSubmit }) => {
    return (
        <div className="generation-status">
            {areFilesReady && (
                <div className="configs-loaded">
                    ✓ Конфигурации загружены и готовы к генерации
                </div>
            )}
            <button 
                onClick={onSubmit} 
                disabled={isGenerating || !areFilesReady}
                className="generate-button"
            >
                {isGenerating ? '[Генерация...]' : '[Сгенерировать проект]'}
            </button>
        </div>
    );
};

export default GenerationControls;