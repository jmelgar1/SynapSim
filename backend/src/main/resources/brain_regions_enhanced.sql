-- =====================================================
-- ENHANCED BRAIN REGIONS SEED DATA
-- Based on Wikipedia's comprehensive list of brain regions
-- Organized by major brain divisions
-- =====================================================
--
-- This file contains a comprehensive database of brain regions
-- extracted from Wikipedia's "List of regions in the human brain"
--
-- Organization:
-- 1. HINDBRAIN (Rhombencephalon)
--    - Myelencephalon (Medulla)
--    - Metencephalon (Pons & Cerebellum)
-- 2. MIDBRAIN (Mesencephalon)
-- 3. FOREBRAIN (Prosencephalon)
--    - Diencephalon (Thalamus, Hypothalamus, Epithalamus)
--    - Telencephalon (Cerebral Cortex, Basal Ganglia, Limbic System)
--
-- Each region includes:
-- - Standardized anatomical name
-- - Common abbreviation/code
-- - Network/system classification
-- - Brief functional description
-- - Baseline activity and neuroplasticity potential estimates
-- - 3D position coordinates for visualization
-- =====================================================

-- =====================================================
-- HINDBRAIN (RHOMBENCEPHALON)
-- =====================================================

-- -----------------------------------------------------
-- MYELENCEPHALON (Medulla Oblongata)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (11, 'Medulla Oblongata', 'MED', 'Most caudal part of the brainstem', 'Controls vital autonomic functions including heart rate, breathing, and blood pressure regulation', 200.0, 500.0, 0.85, 0.45, 'Brainstem, Autonomic');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (12, 'Locus Coeruleus', 'LC', 'Small nucleus in the pons, primary source of norepinephrine', 'Major noradrenergic nucleus; modulates arousal, attention, and stress responses. Critical for psychedelic effects', 210.0, 470.0, 0.70, 0.80, 'Noradrenergic System, Arousal');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (13, 'Dorsal Raphe Nucleus', 'DRN', 'Major serotonergic nucleus in the midbrain', 'Primary source of serotonin; key target of psychedelics. Modulates mood, anxiety, and perception', 200.0, 450.0, 0.75, 0.90, 'Serotonergic System, Brainstem');

-- -----------------------------------------------------
-- METENCEPHALON (Pons)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (14, 'Pons', 'PONS', 'Part of the brainstem connecting medulla and midbrain', 'Relays signals between cerebrum and cerebellum; involved in arousal, sleep, and respiration', 200.0, 480.0, 0.80, 0.50, 'Brainstem');

-- -----------------------------------------------------
-- CEREBELLUM
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (15, 'Cerebellar Vermis', 'VERMIS', 'Midline structure of the cerebellum', 'Motor coordination and postural control; cognitive and emotional processing', 200.0, 420.0, 0.65, 0.70, 'Cerebellar Networks');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (16, 'Dentate Nucleus', 'DN', 'Largest of the deep cerebellar nuclei', 'Motor planning and timing; cognitive functions via connections to prefrontal cortex', 180.0, 410.0, 0.60, 0.65, 'Cerebellar Networks');

-- =====================================================
-- MIDBRAIN (MESENCEPHALON)
-- =====================================================

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (17, 'Periaqueductal Gray', 'PAG', 'Gray matter around the cerebral aqueduct', 'Pain modulation, defensive behaviors, and emotional responses. Altered by psychedelics', 200.0, 440.0, 0.68, 0.75, 'Pain Modulation, Emotional');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (18, 'Ventral Tegmental Area', 'VTA', 'Dopaminergic nucleus in the midbrain', 'Key part of reward circuit; source of mesocorticolimbic dopamine. Influences motivation and reward processing', 190.0, 450.0, 0.72, 0.85, 'Dopaminergic System, Reward');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (19, 'Substantia Nigra', 'SN', 'Basal ganglia structure in the midbrain', 'Dopamine production for motor control and reward; degeneration causes Parkinsons', 185.0, 455.0, 0.70, 0.60, 'Dopaminergic System, Motor');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (20, 'Superior Colliculus', 'SC', 'Part of the tectum in the midbrain', 'Visual and multisensory integration for orienting eye and head movements', 205.0, 445.0, 0.73, 0.60, 'Sensory Integration, Visual');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (21, 'Inferior Colliculus', 'IC', 'Part of the tectum in the midbrain', 'Auditory relay and processing; integrates auditory information', 195.0, 448.0, 0.72, 0.58, 'Auditory System');

-- =====================================================
-- FOREBRAIN (PROSENCEPHALON)
-- =====================================================

-- -----------------------------------------------------
-- DIENCEPHALON - Epithalamus
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (22, 'Pineal Gland', 'PIN', 'Endocrine gland in the epithalamus', 'Produces melatonin; regulates circadian rhythms and sleep-wake cycles', 200.0, 360.0, 0.55, 0.50, 'Circadian, Endocrine');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (23, 'Habenular Nuclei', 'HB', 'Pair of small nuclei in the epithalamus', 'Links forebrain to midbrain; involved in pain, reward, and aversion. Modulates serotonin and dopamine', 205.0, 355.0, 0.63, 0.70, 'Limbic System, Reward');

-- -----------------------------------------------------
-- DIENCEPHALON - Thalamus (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (24, 'Medial Dorsal Nucleus', 'MD', 'Thalamic nucleus with prefrontal connections', 'Relays information to prefrontal cortex; involved in emotion, cognition, and memory', 195.0, 290.0, 0.70, 0.75, 'Thalamo-Cortical, Cognitive');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (25, 'Pulvinar', 'PUL', 'Largest thalamic nucleus', 'Higher-order visual processing and attention; integrates information across cortical areas', 210.0, 285.0, 0.68, 0.70, 'Thalamo-Cortical, Visual, Attention');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (26, 'Lateral Geniculate Nucleus', 'LGN', 'Visual relay nucleus in the thalamus', 'Primary relay for visual information from retina to visual cortex', 215.0, 295.0, 0.78, 0.60, 'Thalamo-Cortical, Visual');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (27, 'Medial Geniculate Nucleus', 'MGN', 'Auditory relay nucleus in the thalamus', 'Primary relay for auditory information to auditory cortex', 190.0, 295.0, 0.75, 0.60, 'Thalamo-Cortical, Auditory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (28, 'Anterior Thalamic Nuclei', 'AN', 'Part of the limbic system in thalamus', 'Memory and spatial navigation; part of Papez circuit for emotion', 200.0, 275.0, 0.66, 0.80, 'Limbic System, Memory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (29, 'Intralaminar Nuclei', 'IL', 'Central thalamic nuclei', 'Arousal and attention; modulates cortical excitability', 200.0, 288.0, 0.72, 0.70, 'Thalamo-Cortical, Arousal');

-- -----------------------------------------------------
-- DIENCEPHALON - Hypothalamus (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (30, 'Suprachiasmatic Nucleus', 'SCN', 'Master circadian pacemaker in hypothalamus', 'Regulates circadian rhythms; receives input from retina', 200.0, 310.0, 0.80, 0.55, 'Circadian, Hypothalamus');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (31, 'Paraventricular Nucleus', 'PVN', 'Neuroendocrine nucleus in hypothalamus', 'Regulates stress response via HPA axis; oxytocin and vasopressin production', 195.0, 308.0, 0.75, 0.70, 'HPA Axis, Stress, Endocrine');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (32, 'Ventromedial Hypothalamus', 'VMH', 'Hypothalamic nucleus for energy regulation', 'Satiety and energy balance; sexual behavior and aggression', 205.0, 312.0, 0.70, 0.65, 'Hypothalamus, Homeostatic');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (33, 'Lateral Hypothalamus', 'LH', 'Lateral region of hypothalamus', 'Feeding behavior and reward; part of pleasure center', 185.0, 310.0, 0.68, 0.75, 'Hypothalamus, Reward, Feeding');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (34, 'Mammillary Bodies', 'MB', 'Part of Papez circuit in hypothalamus', 'Memory processing; part of limbic system for spatial and episodic memory', 200.0, 318.0, 0.64, 0.78, 'Limbic System, Memory');

-- -----------------------------------------------------
-- TELENCEPHALON - Basal Ganglia (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (35, 'Putamen', 'PUT', 'Part of the dorsal striatum', 'Motor control and motor learning; habit formation', 230.0, 270.0, 0.65, 0.70, 'Basal Ganglia, Motor');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (36, 'Caudate Nucleus', 'CD', 'Part of the dorsal striatum', 'Goal-directed action, motor control, and learning', 220.0, 265.0, 0.67, 0.72, 'Basal Ganglia, Cognitive');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (37, 'Nucleus Accumbens', 'NAcc', 'Part of ventral striatum, reward center', 'Reward, pleasure, motivation, and addiction. Key target of dopamine release', 210.0, 300.0, 0.70, 0.88, 'Reward System, Limbic');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (38, 'Globus Pallidus', 'GP', 'Basal ganglia output structure', 'Motor control via inhibition of thalamus; regulates movement initiation', 225.0, 275.0, 0.62, 0.60, 'Basal Ganglia, Motor');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (39, 'Subthalamic Nucleus', 'STN', 'Small nucleus in basal ganglia', 'Motor control; target for deep brain stimulation in Parkinsons disease', 215.0, 295.0, 0.68, 0.65, 'Basal Ganglia, Motor');

-- -----------------------------------------------------
-- TELENCEPHALON - Basal Forebrain
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (40, 'Nucleus Basalis', 'NB', 'Major cholinergic nucleus in basal forebrain', 'Primary source of acetylcholine to cortex; critical for attention and learning', 190.0, 305.0, 0.72, 0.80, 'Cholinergic System, Attention');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (41, 'Septal Nuclei', 'SN', 'Part of limbic system in basal forebrain', 'Reward, reinforcement, and spatial memory; modulates hippocampal function', 195.0, 300.0, 0.65, 0.75, 'Limbic System, Memory');

-- -----------------------------------------------------
-- TELENCEPHALON - Hippocampal Formation (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (42, 'Dentate Gyrus', 'DG', 'Part of hippocampal formation', 'Neurogenesis site; pattern separation in memory formation', 175.0, 325.0, 0.60, 0.95, 'Hippocampal Formation, Memory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (43, 'CA1 Hippocampus', 'CA1', 'Subfield of hippocampus proper', 'Memory consolidation and retrieval; vulnerable to stress', 178.0, 322.0, 0.63, 0.90, 'Hippocampal Formation, Memory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (44, 'CA3 Hippocampus', 'CA3', 'Subfield of hippocampus proper', 'Pattern completion and associative memory; recurrent connections', 182.0, 318.0, 0.64, 0.92, 'Hippocampal Formation, Memory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (45, 'Entorhinal Cortex', 'EC', 'Gateway to hippocampus', 'Spatial memory and navigation; grid cells for spatial representation', 170.0, 330.0, 0.66, 0.85, 'Hippocampal Formation, Memory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (46, 'Parahippocampal Cortex', 'PHC', 'Cortex surrounding hippocampus', 'Scene and context memory; place recognition', 165.0, 335.0, 0.64, 0.80, 'Hippocampal Formation, Memory');

-- -----------------------------------------------------
-- TELENCEPHALON - Amygdala Complex (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (47, 'Basolateral Amygdala', 'BLA', 'Main input station of amygdala', 'Emotional learning and fear conditioning; cortical-like structure', 105.0, 285.0, 0.74, 0.88, 'Limbic System, Emotional');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (48, 'Central Amygdala', 'CeA', 'Output station of amygdala', 'Orchestrates fear and stress responses; autonomic and behavioral outputs', 108.0, 282.0, 0.76, 0.82, 'Limbic System, Autonomic');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (49, 'Bed Nucleus of Stria Terminalis', 'BNST', 'Extended amygdala structure', 'Sustained anxiety and stress; distinct from fear (amygdala proper)', 110.0, 295.0, 0.70, 0.80, 'Extended Amygdala, Anxiety');

-- -----------------------------------------------------
-- TELENCEPHALON - Insula
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (50, 'Anterior Insula', 'AI', 'Front part of insular cortex', 'Interoception, emotional awareness, and empathy. Key for psychedelic emotional effects', 120.0, 220.0, 0.72, 0.85, 'Salience Network, Interoception');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (51, 'Posterior Insula', 'PI', 'Back part of insular cortex', 'Primary interoceptive cortex; body state representation', 115.0, 230.0, 0.70, 0.75, 'Salience Network, Interoception');

-- -----------------------------------------------------
-- TELENCEPHALON - Cingulate Cortex (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (52, 'Anterior Cingulate Cortex', 'ACC', 'Front part of cingulate cortex', 'Conflict monitoring, error detection, and emotion regulation', 200.0, 120.0, 0.73, 0.82, 'Salience Network, Executive');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (53, 'Subgenual Anterior Cingulate', 'sgACC', 'Ventral part of ACC (Brodmann area 25)', 'Depression and negative affect; target for depression treatments', 200.0, 130.0, 0.75, 0.90, 'Default Mode Network, Emotional');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (54, 'Retrosplenial Cortex', 'RSC', 'Posterior cingulate transition zone', 'Episodic memory, navigation, and scene processing', 155.0, 145.0, 0.67, 0.82, 'Default Mode Network, Memory');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (55, 'Precuneus', 'PCUN', 'Medial parietal cortex, part of DMN', 'Self-referential processing, episodic memory, and consciousness. Core DMN hub', 200.0, 160.0, 0.69, 0.83, 'Default Mode Network');

-- -----------------------------------------------------
-- TELENCEPHALON - Prefrontal Cortex (Expanded)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (56, 'Dorsolateral Prefrontal Cortex', 'dlPFC', 'Lateral part of PFC (BA 9, 46)', 'Working memory, cognitive control, and executive function', 250.0, 90.0, 0.71, 0.80, 'Executive Network, Cognitive');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (57, 'Ventromedial Prefrontal Cortex', 'vmPFC', 'Medial ventral PFC (BA 10, 11)', 'Value-based decision making and emotion regulation', 200.0, 90.0, 0.68, 0.85, 'Default Mode Network, Decision Making');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (58, 'Orbitofrontal Cortex', 'OFC', 'Orbital surface of frontal lobe', 'Reward processing, decision making, and impulse control', 205.0, 95.0, 0.70, 0.82, 'Reward System, Decision Making');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (59, 'Ventrolateral Prefrontal Cortex', 'vlPFC', 'Lateral ventral PFC (BA 44, 45, 47)', 'Response inhibition and emotion regulation', 240.0, 100.0, 0.69, 0.78, 'Executive Network, Inhibition');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (60, 'Dorsomedial Prefrontal Cortex', 'dmPFC', 'Medial dorsal PFC (BA 8, 9)', 'Social cognition, mentalizing, and self-reference', 200.0, 85.0, 0.70, 0.84, 'Default Mode Network, Social');

-- -----------------------------------------------------
-- TELENCEPHALON - Motor and Premotor Cortex
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (61, 'Primary Motor Cortex', 'M1', 'Precentral gyrus (BA 4)', 'Voluntary motor control; sends commands to spinal cord', 260.0, 140.0, 0.78, 0.75, 'Motor Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (62, 'Premotor Cortex', 'PMC', 'Anterior to M1 (BA 6)', 'Motor planning and preparation; sensorimotor integration', 265.0, 130.0, 0.72, 0.78, 'Motor Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (63, 'Supplementary Motor Area', 'SMA', 'Medial BA 6', 'Complex motor sequences and bimanual coordination', 200.0, 110.0, 0.70, 0.80, 'Motor Network');

-- -----------------------------------------------------
-- TELENCEPHALON - Somatosensory Cortex
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (64, 'Primary Somatosensory Cortex', 'S1', 'Postcentral gyrus (BA 1, 2, 3)', 'Tactile and proprioceptive sensations from body', 270.0, 160.0, 0.76, 0.72, 'Somatosensory Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (65, 'Secondary Somatosensory Cortex', 'S2', 'Superior bank of Sylvian fissure', 'Higher-order tactile processing; bilateral representation', 265.0, 170.0, 0.70, 0.70, 'Somatosensory Network');

-- -----------------------------------------------------
-- TELENCEPHALON - Parietal Cortex
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (66, 'Posterior Parietal Cortex', 'PPC', 'BA 5, 7, 39, 40', 'Spatial attention, visuomotor integration, and action planning', 285.0, 175.0, 0.68, 0.76, 'Dorsal Attention Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (67, 'Inferior Parietal Lobule', 'IPL', 'BA 39, 40', 'Language, mathematical cognition, and body image', 290.0, 180.0, 0.67, 0.75, 'Frontoparietal Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (68, 'Superior Parietal Lobule', 'SPL', 'BA 5, 7', 'Spatial processing and visuomotor coordination', 280.0, 165.0, 0.66, 0.74, 'Dorsal Attention Network');

-- -----------------------------------------------------
-- TELENCEPHALON - Temporal Cortex
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (69, 'Superior Temporal Gyrus', 'STG', 'Includes auditory cortex (BA 22, 41, 42)', 'Auditory processing and language comprehension', 90.0, 250.0, 0.74, 0.70, 'Auditory Network, Language');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (70, 'Middle Temporal Gyrus', 'MTG', 'BA 21, 37', 'Semantic memory and language processing', 85.0, 260.0, 0.68, 0.72, 'Language, Semantic Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (71, 'Inferior Temporal Gyrus', 'ITG', 'BA 20', 'Object recognition and face processing', 80.0, 270.0, 0.70, 0.74, 'Ventral Visual Stream');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (72, 'Fusiform Gyrus', 'FG', 'BA 37', 'Face and body recognition; visual word form area', 75.0, 280.0, 0.72, 0.76, 'Ventral Visual Stream');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (73, 'Temporal Pole', 'TP', 'BA 38', 'Social and emotional processing; semantic memory', 70.0, 240.0, 0.65, 0.78, 'Social Brain, Semantic');

-- -----------------------------------------------------
-- TELENCEPHALON - Occipital Cortex (Visual)
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (74, 'V2 Visual Cortex', 'V2', 'Secondary visual cortex (BA 18)', 'Basic visual features and contour processing', 48.0, 205.0, 0.76, 0.68, 'Visual Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (75, 'V3 Visual Cortex', 'V3', 'Third visual area', 'Dynamic form perception and depth', 45.0, 210.0, 0.74, 0.66, 'Visual Network');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (76, 'V4 Visual Cortex', 'V4', 'Fourth visual area', 'Color processing and shape recognition', 52.0, 208.0, 0.75, 0.67, 'Visual Network, Ventral Stream');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (77, 'V5/MT Visual Cortex', 'V5', 'Middle temporal visual area', 'Motion processing and visual guidance of movement', 55.0, 215.0, 0.77, 0.65, 'Visual Network, Dorsal Stream');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (78, 'Cuneus', 'CUN', 'Medial occipital cortex', 'Visual processing of the contralateral lower visual field', 200.0, 190.0, 0.73, 0.68, 'Visual Network');

-- -----------------------------------------------------
-- TELENCEPHALON - Other Cortical Regions
-- -----------------------------------------------------

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (79, 'Claustrum', 'CLA', 'Thin sheet of gray matter', 'Consciousness and multisensory integration; connects widely to cortex', 150.0, 220.0, 0.68, 0.82, 'Consciousness, Integration');

INSERT INTO brain_regions (id, name, code, description, function_description, positionx, positiony, baseline_activity, neuroplasticity_potential, network)
VALUES (80, 'Piriform Cortex', 'PIR', 'Primary olfactory cortex', 'Odor identification and olfactory memory', 130.0, 295.0, 0.64, 0.72, 'Olfactory System');

-- Keep existing 10 original regions (IDs 1-10) as they were
-- These are already defined in the original import.sql

-- =====================================================
-- END OF ENHANCED BRAIN REGIONS
-- Total: 80 regions (70 new + 10 original)
-- =====================================================
