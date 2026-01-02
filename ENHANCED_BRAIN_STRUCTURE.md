# Enhanced Brain Structure for SynapSim

## Overview

This document describes the enhanced brain region database for SynapSim, expanding from 10 core regions to **80 comprehensive brain regions** based on Wikipedia's authoritative list.

## What's Included

The enhanced structure (`brain_regions_enhanced.sql`) contains regions organized by major brain divisions:

### 1. **Hindbrain (Rhombencephalon)** - 6 regions
- **Medulla Oblongata**: Autonomic control
- **Locus Coeruleus**: Primary norepinephrine source (critical for psychedelics)
- **Dorsal Raphe Nucleus**: Primary serotonin source (key psychedelic target)
- **Pons**: Arousal and relay
- **Cerebellar Vermis** & **Dentate Nucleus**: Motor and cognitive coordination

### 2. **Midbrain (Mesencephalon)** - 6 regions
- **Periaqueductal Gray (PAG)**: Pain modulation, emotional responses
- **Ventral Tegmental Area (VTA)**: Dopamine reward circuit
- **Substantia Nigra**: Dopamine for motor control
- **Superior/Inferior Colliculi**: Sensory integration
- **Dorsal Raphe Nucleus**: Serotonin (already listed in hindbrain section)

### 3. **Forebrain (Prosencephalon)** - 68 regions

#### Diencephalon (12 regions)
**Epithalamus:**
- Pineal Gland, Habenular Nuclei

**Thalamus (Expanded):**
- Medial Dorsal Nucleus, Pulvinar, Lateral/Medial Geniculate Nuclei
- Anterior Thalamic Nuclei, Intralaminar Nuclei

**Hypothalamus (Expanded):**
- Suprachiasmatic Nucleus (circadian), Paraventricular Nucleus (HPA axis)
- Ventromedial/Lateral Hypothalamus, Mammillary Bodies

#### Telencephalon - Subcortical (21 regions)
**Basal Ganglia:**
- Putamen, Caudate, Nucleus Accumbens (reward!)
- Globus Pallidus, Subthalamic Nucleus

**Basal Forebrain:**
- Nucleus Basalis (acetylcholine), Septal Nuclei

**Hippocampal Formation:**
- Dentate Gyrus, CA1, CA3, Entorhinal Cortex, Parahippocampal Cortex

**Amygdala Complex:**
- Basolateral/Central Amygdala, BNST (extended amygdala)

#### Telencephalon - Cortical (35 regions)
**Insula:**
- Anterior/Posterior Insula (interoception, empathy)

**Cingulate:**
- ACC, Subgenual ACC (BA25 - depression target), Retrosplenial, Precuneus

**Prefrontal Cortex:**
- dlPFC, vmPFC, OFC, vlPFC, dmPFC

**Motor/Somatosensory:**
- M1, PMC, SMA, S1, S2

**Parietal:**
- PPC, IPL, SPL

**Temporal:**
- STG, MTG, ITG, Fusiform, Temporal Pole

**Occipital (Visual):**
- V2, V3, V4, V5/MT, Cuneus

**Other:**
- Claustrum (consciousness), Piriform (olfactory)

## Key Features

### Psychedelic-Relevant Regions
The enhanced database emphasizes regions critical for psychedelic research:
- **5-HT2A receptor-rich areas**: mPFC, PCC, Claustrum, Piriform
- **DMN hubs**: mPFC, PCC, Precuneus, ACC, vmPFC
- **Serotonergic nuclei**: Dorsal Raphe Nucleus
- **Reward circuit**: VTA, Nucleus Accumbens, OFC
- **Emotional processing**: Amygdala complex, Insula, sgACC

### Each Region Includes:
- **Standardized name** from anatomical literature
- **Code/Abbreviation** (e.g., dlPFC, VTA, NAcc)
- **Network classification** (DMN, Salience, Executive, etc.)
- **Functional description** specific to psychedelic/neuroscience context
- **Baseline activity** (0.0-1.0 estimate)
- **Neuroplasticity potential** (0.0-1.0 - how responsive to psychedelics)
- **3D coordinates** for visualization

## How to Use

### Option 1: Replace Original Data (Clean Start)
```bash
# Backend directory
cd backend

# Replace import.sql with enhanced version
cp src/main/resources/brain_regions_enhanced.sql src/main/resources/import.sql

# Drop and recreate database
# Then restart application
./mvnw spring-boot:run
```

### Option 2: Append to Existing Data
```sql
-- Keep your original 10 regions (IDs 1-10)
-- Add the new 70 regions from brain_regions_enhanced.sql (IDs 11-80)
-- This maintains backward compatibility
```

### Option 3: Selective Import
Choose specific regions relevant to your use case:
```sql
-- Example: Import only psychedelic-critical regions
-- DMN + Serotonergic + Reward + Emotional
INSERT INTO brain_regions ... -- DRN
INSERT INTO brain_regions ... -- VTA
INSERT INTO brain_regions ... -- NAcc
INSERT INTO brain_regions ... -- sgACC
-- etc.
```

## Updating the Alias Map

To enable research article matching for all new regions, update `BrainNetworkService.buildRegionAliasMap()`:

```java
private Map<String, List<String>> buildRegionAliasMap() {
    Map<String, List<String>> aliases = new HashMap<>();

    // Original 10 regions (already defined)
    aliases.put("mPFC", List.of("medial prefrontal cortex", "mpfc", "prefrontal", "pfc"));
    // ... existing entries ...

    // NEW REGIONS - Add these:

    // Brainstem
    aliases.put("DRN", List.of("dorsal raphe", "raphe nucleus", "raphe nuclei", "serotonergic"));
    aliases.put("LC", List.of("locus coeruleus", "locus ceruleus", "noradrenergic", "norepinephrine"));
    aliases.put("PAG", List.of("periaqueductal gray", "periaqueductal grey", "pag"));

    // Midbrain
    aliases.put("VTA", List.of("ventral tegmental area", "ventral tegmental", "vta"));
    aliases.put("SN", List.of("substantia nigra", "nigra"));

    // Thalamus
    aliases.put("MD", List.of("medial dorsal", "mediodorsal", "md thalamus"));
    aliases.put("PUL", List.of("pulvinar", "pulvinar nucleus"));

    // Basal Ganglia
    aliases.put("NAcc", List.of("nucleus accumbens", "accumbens", "ventral striatum", "nacc"));
    aliases.put("PUT", List.of("putamen"));
    aliases.put("CD", List.of("caudate", "caudate nucleus"));

    // Hippocampus
    aliases.put("DG", List.of("dentate gyrus", "dentate"));
    aliases.put("CA1", List.of("ca1", "cornu ammonis 1", "hippocampus ca1"));
    aliases.put("CA3", List.of("ca3", "cornu ammonis 3", "hippocampus ca3"));
    aliases.put("EC", List.of("entorhinal", "entorhinal cortex"));

    // Prefrontal
    aliases.put("dlPFC", List.of("dorsolateral prefrontal", "dlpfc", "lateral prefrontal"));
    aliases.put("vmPFC", List.of("ventromedial prefrontal", "vmpfc", "medial prefrontal ventral"));
    aliases.put("OFC", List.of("orbitofrontal", "orbital frontal", "ofc"));
    aliases.put("sgACC", List.of("subgenual", "subgenual anterior cingulate", "area 25", "ba25"));

    // Insula
    aliases.put("AI", List.of("anterior insula", "insular cortex anterior"));
    aliases.put("PI", List.of("posterior insula", "insular cortex posterior"));

    // Cingulate
    aliases.put("ACC", List.of("anterior cingulate", "acc", "anterior cingulate cortex"));
    aliases.put("PCUN", List.of("precuneus", "medial parietal"));

    // Add more as needed...

    return aliases;
}
```

## Neural Connections

The enhanced structure provides a foundation for **richer connectivity patterns**. Consider adding connections for:

1. **Serotonergic projections**: DRN → mPFC, PCC, V1, Claustrum
2. **Dopaminergic pathways**: VTA → NAcc, mPFC (mesocorticolimbic)
3. **Default Mode Network**: mPFC ↔ PCC ↔ Precuneus ↔ IPL
4. **Salience Network**: AI ↔ ACC
5. **Hippocampal-cortical**: CA1 → EC → mPFC
6. **Reward circuitry**: VTA → NAcc → OFC → vmPFC

## Benefits of Enhanced Structure

✅ **More accurate research matching**: PubMed articles mention specific subregions
✅ **Better visualization**: Richer 3D brain networks
✅ **Educational value**: Users learn comprehensive neuroanatomy
✅ **Future-proof**: Can add connections as research emerges
✅ **Psychedelic-optimized**: Includes all key targets of psychedelics

## Considerations

⚠️ **Graph complexity**: 80 nodes means potentially 3,160 connections (if fully connected)
⚠️ **Database size**: Larger seed data
⚠️ **UI updates**: May need to handle variable network sizes
⚠️ **Research filtering**: Implement the dynamic region filtering discussed earlier

## Recommended Approach

**Phase 1** (MVP+): Use current 10 regions + add 10-15 most critical:
- DRN, VTA, NAcc, sgACC, AI, dlPFC, vmPFC, OFC, Precuneus, CA1

**Phase 2** (Future): Full 80-region implementation with dynamic filtering

**Phase 3** (Advanced): User-selectable region sets (DMN only, Reward only, etc.)

## References

- Wikipedia: List of regions in the human brain
- Carhart-Harris et al. (2012): Neural correlates of the psychedelic state
- Preller & Vollenweider (2018): Psychedelics and the human brain
- Deco & Kringelbach (2014): Brain connectivity dynamics

---

**Next Steps:**
1. Review which regions are most important for your use cases
2. Decide on implementation approach (replace, append, selective)
3. Update `BrainNetworkService` alias mapping
4. Add relevant neural connections
5. Test with PubMed API to verify research matching
