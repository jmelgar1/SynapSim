-- Brain Regions Seed Data
-- These represent key brain regions involved in psychedelic research

INSERT INTO brain_regions (id, name, code, description, function_description, position_x, position_y, baseline_activity, neuroplasticity_potential) VALUES
(1, 'Prefrontal Cortex', 'PFC', 'The prefrontal cortex is the cerebral cortex covering the front part of the frontal lobe.', 'Executive functions, decision-making, planning, impulse control, and emotional regulation.', 200.0, 100.0, 0.65, 0.75),
(2, 'Amygdala', 'AMY', 'An almond-shaped structure deep in the brain, part of the limbic system.', 'Processing emotions, especially fear and anxiety responses; emotional memory formation.', 100.0, 300.0, 0.70, 0.80),
(3, 'Hippocampus', 'HPC', 'A curved structure in the medial temporal lobe, critical for memory.', 'Memory formation, consolidation, and spatial navigation; stress response regulation.', 150.0, 350.0, 0.60, 0.85),
(4, 'Anterior Cingulate Cortex', 'ACC', 'A collar-shaped region surrounding the corpus callosum.', 'Conflict monitoring, error detection, emotional regulation, and empathy.', 250.0, 200.0, 0.55, 0.70),
(5, 'Insula', 'INS', 'A region located deep within the lateral sulcus of the brain.', 'Interoception, emotional awareness, empathy, and self-awareness.', 300.0, 250.0, 0.50, 0.65),
(6, 'Default Mode Network Hub', 'DMN', 'A network of interacting brain regions active during rest and self-referential thinking.', 'Self-reflection, autobiographical memory, theory of mind, and mind-wandering.', 200.0, 50.0, 0.75, 0.60),
(7, 'Thalamus', 'THL', 'A large mass of gray matter in the dorsal part of the diencephalon.', 'Sensory relay station, consciousness regulation, and sleep-wake cycles.', 200.0, 300.0, 0.80, 0.55),
(8, 'Ventral Tegmental Area', 'VTA', 'A group of neurons located in the midbrain.', 'Reward processing, motivation, and dopamine release; pleasure and reinforcement.', 350.0, 350.0, 0.60, 0.70),
(9, 'Posterior Cingulate Cortex', 'PCC', 'A posterior part of the cingulate cortex, connected to the DMN.', 'Episodic memory retrieval, self-referential processing, and consciousness.', 150.0, 150.0, 0.65, 0.65),
(10, 'Visual Cortex', 'V1', 'The primary visual cortex located in the occipital lobe.', 'Processing visual information; altered in psychedelic experiences.', 50.0, 200.0, 0.70, 0.50);

-- Neural Connections Seed Data
-- Baseline connections between brain regions with their default strengths

-- PFC connections (executive control)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(1, 2, 0.60, 0.60, 'INHIBITORY', true, 'PFC regulates amygdala activity, reducing emotional reactivity'),
(1, 4, 0.70, 0.70, 'EXCITATORY', true, 'PFC and ACC work together for cognitive control'),
(1, 6, 0.55, 0.55, 'MODULATORY', true, 'PFC modulates DMN during task-focused activities'),
(1, 7, 0.65, 0.65, 'EXCITATORY', true, 'PFC-thalamus connection for attention and awareness');

-- Amygdala connections (emotional processing)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(2, 3, 0.75, 0.75, 'EXCITATORY', true, 'Amygdala-hippocampus connection for emotional memory formation'),
(2, 4, 0.65, 0.65, 'EXCITATORY', true, 'Emotional signals to ACC for conflict monitoring'),
(2, 5, 0.70, 0.70, 'EXCITATORY', true, 'Amygdala-insula connection for emotional awareness'),
(2, 7, 0.80, 0.80, 'EXCITATORY', true, 'Amygdala receives sensory input from thalamus');

-- Hippocampus connections (memory and stress)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(3, 6, 0.70, 0.70, 'EXCITATORY', true, 'Hippocampus-DMN connection for autobiographical memory'),
(3, 9, 0.65, 0.65, 'EXCITATORY', true, 'Hippocampus-PCC for memory retrieval'),
(3, 1, 0.55, 0.55, 'MODULATORY', true, 'Hippocampus provides context to PFC for decision-making');

-- ACC connections (emotional regulation and empathy)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(4, 5, 0.75, 0.75, 'EXCITATORY', true, 'ACC-insula connection crucial for empathy and emotional awareness'),
(4, 6, 0.60, 0.60, 'MODULATORY', true, 'ACC monitors DMN activity for self-reflection'),
(4, 8, 0.55, 0.55, 'EXCITATORY', true, 'ACC-VTA connection for motivation and reward processing');

-- Insula connections (interoception and self-awareness)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(5, 6, 0.65, 0.65, 'EXCITATORY', true, 'Insula-DMN for self-awareness and introspection'),
(5, 7, 0.60, 0.60, 'EXCITATORY', true, 'Insula receives sensory signals from thalamus');

-- DMN connections (self-referential thinking)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(6, 9, 0.80, 0.80, 'EXCITATORY', true, 'Core DMN connection between hub and PCC'),
(6, 7, 0.50, 0.50, 'MODULATORY', true, 'DMN-thalamus connection modulated during consciousness shifts');

-- VTA connections (reward and motivation)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(8, 1, 0.70, 0.70, 'MODULATORY', true, 'VTA provides dopaminergic input to PFC for motivation'),
(8, 3, 0.60, 0.60, 'MODULATORY', true, 'VTA-hippocampus for reward-based memory consolidation');

-- Visual cortex connections (perception)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(10, 7, 0.85, 0.85, 'EXCITATORY', true, 'Thalamus relays visual information to V1'),
(10, 6, 0.45, 0.45, 'MODULATORY', true, 'Visual cortex-DMN connection altered in psychedelic states');

-- PCC connections (memory and consciousness)
INSERT INTO neural_connections (source_region_id, target_region_id, weight, baseline_weight, connection_type, is_bidirectional, description) VALUES
(9, 1, 0.55, 0.55, 'MODULATORY', true, 'PCC-PFC connection for integrating memory with decision-making'),
(9, 7, 0.60, 0.60, 'EXCITATORY', true, 'PCC-thalamus connection for conscious awareness');

-- Reset sequence for IDs
ALTER SEQUENCE brain_regions_id_seq RESTART WITH 11;
ALTER SEQUENCE neural_connections_id_seq RESTART WITH 100;
