let network = null;

function initGraph(dataUrl, layout, onNodeClick) {
    if (network !== null) {
        network.destroy();
        network = null;
    }

    const container = document.getElementById('graph-container');

    $.getJSON(dataUrl, function (graphData) {
        const nodes = new vis.DataSet(graphData.nodes.map(n => ({
            id: n.id,
            label: n.label,
            group: n.type,
            title: n.title,
            shape: 'icon',
            icon: {
                face: "'Material Symbols Outlined'",
                code: n.icon,
                size: 50,
                color: getNodeColor(n.type)
            }
        })));

        const edges = new vis.DataSet(graphData.edges);

        const data = { nodes: nodes, edges: edges };
        const options = getGraphOptions(layout);

        network = new vis.Network(container, data, options);

        network.on("click", function (params) {
            if (params.nodes.length > 0) {
                const nodeId = params.nodes[0];
                if (onNodeClick) {
                    onNodeClick(nodeId);
                }
            }
        });

        $('#exportPngBtn').off('click').on('click', function() {
             const canvas = container.getElementsByTagName('canvas')[0];
             const dataURL = canvas.toDataURL('image/png');
             const link = document.createElement('a');
             link.href = dataURL;
             link.download = 'graph.png';
             link.click();
        });
    });
}

function getGraphOptions(layout) {
    const options = {
        nodes: {
            font: {
                size: 12,
                color: '#333'
            },
            borderWidth: 2,
        },
        edges: {
            width: 2,
            arrows: 'to',
            font: {
                align: 'top'
            }
        },
        physics: {
            enabled: layout === 'force',
            forceAtlas2Based: {
                gravitationalConstant: -50,
                centralGravity: 0.01,
                springConstant: 0.08,
                springLength: 100,
                damping: 0.4,
                avoidOverlap: 0
            },
            solver: 'forceAtlas2Based',
        },
        layout: {
            hierarchical: {
                enabled: layout === 'hierarchical',
                direction: 'LR', // Left to Right
                sortMethod: 'hubsize'
            }
        },
        groups: getNodeGroups()
    };
    return options;
}

function getNodeColor(nodeType) {
    const colors = {
        WEB: '#0ea5e9', APP: '#8b5cf6', DATABASE: '#22c55e', DATABASE_ORACLE: '#f97316',
        CACHE: '#06b6d4', QUEUE: '#f59e0b', KAFKA: '#7c3aed', ELK: '#10b981', UNKNOWN: '#64748b'
    };
    return colors[nodeType] || '#64748b';
}

function getNodeGroups() {
    const nodeTypes = {
        WEB: { color: '#0ea5e9', icon: { code: 'public' } },
        APP: { color: '#8b5cf6', icon: { code: 'developer_board' } },
        DATABASE: { color: '#22c55e', icon: { code: 'database' } },
        DATABASE_ORACLE: { color: '#f97316', icon: { code: 'storage' } },
        CACHE: { color: '#06b6d4', icon: { code: 'bolt' } },
        QUEUE: { color: '#f59e0b', icon: { code: 'sync_alt' } },
        KAFKA: { color: '#7c3aed', icon: { code: 'hub' } },
        ELK: { color: '#10b981', icon: { code: 'search' } },
        UNKNOWN: { color: '#64748b', icon: { code: 'device_unknown' } }
    };

    let groups = {};
    for (const [type, style] of Object.entries(nodeTypes)) {
        groups[type] = {
            shape: 'icon',
            icon: {
                face: "'Material Symbols Outlined'",
                code: style.icon.code,
                size: 50,
                color: style.color
            }
        };
    }
    return groups;
}
