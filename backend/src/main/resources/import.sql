-- Brain Regions Seed Data
-- These represent key brain regions involved in psychedelic research
-- Based on documented psychedelic neuroscience research

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (1, 'Medial Prefrontal Cortex', 'mPFC', 'Key hub of the Default Mode Network in the frontal lobe.', 'Hub for self-awareness; shows decreased activity and decoupling, promoting flexible thinking.', 200.0, 100.0, 0.70, 0.85, 'Default Mode Network');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (2, 'Posterior Cingulate Cortex', 'PCC', 'A posterior part of the cingulate cortex, central to the DMN.', 'Integrates personal experiences; connectivity weakens, linked to ego dissolution.', 150.0, 150.0, 0.68, 0.80, 'Default Mode Network');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (3, 'Anterior Hippocampus', 'AHP', 'Anterior portion of the hippocampus, critical for memory and DMN connectivity.', 'Handles memory; reduced functional connectivity with DMN during and post-trip, aiding insights.', 180.0, 320.0, 0.62, 0.90, 'Default Mode Network');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (4, 'Amygdala', 'AMY', 'Almond-shaped structure deep in the brain, part of the limbic system.', 'Processes emotions; decreased reactivity and altered connectivity, reducing fear.', 100.0, 280.0, 0.72, 0.85, 'Salience Network, Limbic System');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (5, 'Visual Cortex', 'V1', 'Primary visual cortex in the occipital lobe.', 'Increased activity and connections, contributing to hallucinations.', 50.0, 200.0, 0.75, 0.70, 'Sensory Networks');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (6, 'Auditory Cortex', 'A1', 'Primary auditory cortex in the temporal lobe.', 'Heightened integration with other areas, enhancing perceptual changes.', 80.0, 240.0, 0.68, 0.72, 'Sensory Networks');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (7, 'Thalamus', 'THL', 'Mediodorsal and paraventricular nuclei of the thalamus.', 'Relays signals; desynchronizes, allowing novel information flow.', 200.0, 280.0, 0.82, 0.65, 'Thalamo-Cortical Networks');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (8, 'Anteromedial Caudate', 'AMC', 'Part of the basal ganglia with DMN connections.', 'Involved in motivation; shows connectivity changes for behavioral flexibility.', 220.0, 260.0, 0.58, 0.75, 'Subcortical Networks, DMN-linked');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (9, 'Frontoparietal Regions', 'FP', 'Distributed regions across frontal and parietal cortices.', 'Attention control; increased integration with sensory and limbic systems.', 280.0, 180.0, 0.65, 0.78, 'Frontoparietal Network');
INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network) VALUES (10, 'Cerebellum', 'CBL', 'DMN-connected parts of the cerebellum.', 'Motor and cognitive roles; exhibits functional connectivity shifts.', 200.0, 400.0, 0.60, 0.68, 'Cerebellar Networks, DMN');

-- Neural Connections Seed Data
-- Research-based connections reflecting psychedelic neuroscience findings
-- Organized by functional networks and their interactions

-- DMN Internal Connections (critical for ego dissolution)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (1, 2, 0.80, 0.80, 'EXCITATORY', true, 'Core DMN: mPFC-PCC connection, weakens during psychedelic states causing ego dissolution');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (1, 3, 0.70, 0.70, 'EXCITATORY', true, 'mPFC-Anterior Hippocampus for self-referential memory integration');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (2, 3, 0.75, 0.75, 'EXCITATORY', true, 'PCC-Anterior Hippocampus for episodic memory retrieval and personal narrative');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (2, 10, 0.60, 0.60, 'MODULATORY', true, 'PCC-Cerebellum DMN connectivity, altered in psychedelic states');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (1, 8, 0.55, 0.55, 'MODULATORY', true, 'mPFC-Anteromedial Caudate for motivation and goal-directed behavior');

-- DMN-to-Sensory Connections (increased in psychedelic states)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (2, 5, 0.40, 0.40, 'MODULATORY', true, 'PCC-Visual Cortex: low baseline, dramatically increases during psychedelic experiences');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (1, 6, 0.35, 0.35, 'MODULATORY', true, 'mPFC-Auditory Cortex: weak baseline, enhanced during altered states');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (3, 5, 0.30, 0.30, 'MODULATORY', true, 'Hippocampus-Visual Cortex for visual memory encoding');

-- Limbic-DMN Connections (emotion-self interface)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (1, 4, 0.65, 0.65, 'INHIBITORY', true, 'mPFC normally inhibits amygdala; this connection weakens under psychedelics');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (4, 3, 0.78, 0.78, 'EXCITATORY', true, 'Amygdala-Anterior Hippocampus for emotional memory consolidation');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (4, 2, 0.50, 0.50, 'EXCITATORY', true, 'Amygdala-PCC connection for emotional context in autobiographical memory');

-- Sensory Network Internal Connections (enhanced integration)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (5, 6, 0.55, 0.55, 'EXCITATORY', true, 'Visual-Auditory cross-modal integration, enhanced in psychedelic states (synesthesia)');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (5, 7, 0.85, 0.85, 'EXCITATORY', true, 'Thalamus-Visual Cortex primary visual relay pathway');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (6, 7, 0.82, 0.82, 'EXCITATORY', true, 'Thalamus-Auditory Cortex primary auditory relay pathway');

-- Thalamic Gating and Desynchronization
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (7, 4, 0.75, 0.75, 'EXCITATORY', true, 'Thalamus-Amygdala for rapid sensory-emotional integration');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (7, 9, 0.68, 0.68, 'EXCITATORY', true, 'Thalamus-Frontoparietal for attention gating and control');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (7, 10, 0.65, 0.65, 'MODULATORY', true, 'Thalamus-Cerebellum for sensorimotor coordination');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (7, 1, 0.60, 0.60, 'MODULATORY', true, 'Thalamus-mPFC for conscious awareness, desynchronizes under psychedelics');

-- Frontoparietal Network Connections (attention control)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (9, 1, 0.70, 0.70, 'EXCITATORY', true, 'Frontoparietal-mPFC for executive attention and cognitive control');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (9, 4, 0.62, 0.62, 'MODULATORY', true, 'Frontoparietal-Amygdala for emotion regulation and attention to emotional stimuli');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (9, 5, 0.58, 0.58, 'EXCITATORY', true, 'Frontoparietal-Visual Cortex for top-down attention control of perception');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (9, 6, 0.55, 0.55, 'EXCITATORY', true, 'Frontoparietal-Auditory Cortex for auditory attention and processing');

-- Subcortical Integration (motivation and motor)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (8, 3, 0.64, 0.64, 'MODULATORY', true, 'Anteromedial Caudate-Hippocampus for motivation-memory interactions');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (8, 2, 0.58, 0.58, 'MODULATORY', true, 'Anteromedial Caudate-PCC for goal-directed DMN modulation');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (10, 3, 0.52, 0.52, 'EXCITATORY', true, 'Cerebellum-Hippocampus for procedural and spatial memory integration');
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES (10, 9, 0.50, 0.50, 'EXCITATORY', true, 'Cerebellum-Frontoparietal for cognitive and motor coordination');
