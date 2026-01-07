# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SynapSim is an educational web application that simulates neural pathway changes inspired by psychedelic research. The app allows users to create personalized scenarios and visualize brain network changes based on real neuroscience research from PubMed. **This is an educational tool, not medical advice.**

**Key Features:**
- Research-driven simulations using PubMed API integration
- Interactive brain network visualization with Cytoscape.js
- Graph-based neural network modeling with 80+ brain regions
- Gamification with quests and progress tracking
- Educational focus on neuroplasticity and mental health

## Technology Stack

### Backend (Spring Boot)
- **Language:** Java 21
- **Framework:** Spring Boot 4.0.0
- **Build Tool:** Maven
- **Database:** PostgreSQL (production), H2 (testing)
- **Graph Library:** JGraphT for neural network modeling
- **Key Dependencies:** Spring Data JPA, WebFlux (for PubMed API), ModelMapper, Lombok

### Frontend (Vue.js)
- **Framework:** Vue 3 (Composition API)
- **Build Tool:** Vite
- **State Management:** Pinia
- **Visualization:** Cytoscape.js for brain network graphs
- **HTTP Client:** Axios
- **Node Version:** ^20.19.0 || >=22.12.0

## Development Commands

### Backend (from `/backend` directory)

```bash
# Build the project
./mvnw clean compile

# Run all tests
./mvnw test

# Run the Spring Boot application (starts on port 8080)
./mvnw spring-boot:run

# Run with test profile (uses H2 in-memory database, minimal logging)
./mvnw spring-boot:run -Dspring-boot.run.profiles=test

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=<profile-name>

# Build JAR file
./mvnw clean package
```

### Frontend (from `/frontend` directory)

```bash
# Install dependencies
npm install

# Run development server (Vite)
npm run dev

# Build for production
npm run build

# Run unit tests (Vitest)
npm run test:unit

# Lint and auto-fix
npm run lint

# Format code with Prettier
npm run format
```

### Database Setup

The backend uses PostgreSQL by default. Connection settings in `backend/src/main/resources/application.properties`:
- **URL:** `jdbc:postgresql://localhost:5432/synapsim`
- **Username:** `postgres`
- **Password:** `password`

Database schema and seed data automatically load from `backend/src/main/resources/import.sql` on startup (JPA `ddl-auto=update`).

**Test Profile:**
For development without PostgreSQL, use the test profile (`application-test.properties`):
- Uses H2 in-memory database (no installation required)
- Fresh schema creation on each startup (`ddl-auto=create-drop`)
- Minimal logging for cleaner console output
- Run with: `./mvnw spring-boot:run -Dspring-boot.run.profiles=test`

## Architecture

### Backend Architecture

**3-Tier Service Layer:**
```
Controller (REST API)
    ↓
SimulationService (Orchestration)
    ↓
BrainNetworkService / PubMedService (Domain Logic)
    ↓
Repository / JGraphT (Data/Graph Processing)
```

**Package Structure:**
- `controller/` - REST API endpoints
- `service/` - Business logic (SimulationService, BrainNetworkService, PubMedService, BrainRegionContextValidator)
- `model/` - JPA entities (BrainRegion, NeuralConnection, Scenario, Simulation, UserProgress, PubMedReference)
- `dto/` - Data Transfer Objects for API responses
- `repository/` - Spring Data JPA repositories
- `config/` - Application configuration (CORS, beans)
- `exception/` - Custom exceptions
- `util/` - Utility classes

**Key Services:**

1. **SimulationService** (`SimulationService.java`)
   - Orchestrates the complete simulation workflow
   - Creates scenarios from user input
   - Fetches research from PubMed API
   - Extracts mentioned brain regions using NLP/NER
   - Builds filtered brain graphs based on research
   - Applies neuroplasticity changes
   - Generates simulation responses

2. **BrainNetworkService** (`BrainNetworkService.java`)
   - Manages JGraphT-based brain network graphs
   - Loads brain regions and connections from database
   - Applies compound-specific and setting-specific modifiers
   - Converts graphs to DTOs for frontend visualization
   - Implements neuroplasticity algorithms with duration multipliers

3. **PubMedService** (`PubMedService.java`)
   - Searches PubMed API with keyword extraction
   - Implements relevance scoring for articles
   - Extracts brain region mentions from abstracts using NLP/NER
   - Matches region aliases (e.g., "amygdala" → "AMY")

4. **BrainRegionContextValidator** (`BrainRegionContextValidator.java`)
   - Validates brain region mentions within research context
   - Prevents false positives in region detection

### Frontend Architecture

**Vue 3 Composition API structure:**
- `views/` - Page-level components (HomeView, Dashboard, OnboardingQuiz, ScenarioBuilder, SimulationResults)
- `components/` - Reusable components
  - `common/` - Shared UI (DisclaimerModal, PrimaryButton, VibrantHomeScreenBackground)
  - `features/` - Feature-specific (BrainNetworkGraph, QuestCard)
- `services/` - API clients (api.js, simulation.js, pubmed.js)
- `composables/` - Vue composables (useUserProgress, useLocalStorage)
- `router/` - Vue Router configuration
- `constants/` - App-wide constants

**State Management:**
- Local storage for user progress and onboarding data (no backend auth yet)
- Pinia for reactive state management

### Data Flow for Simulations

1. **User Input** (ScenarioBuilder.vue) → POST `/api/simulations/run`
2. **SimulationService** extracts keywords, queries PubMed API
3. **PubMedService** searches research, scores relevance
4. **Brain Region Detection** using NLP/NER on abstracts
5. **BrainNetworkService** builds filtered graph with only mentioned regions
6. **Neuroplasticity Algorithm** applies compound/setting modifiers
7. **Response** includes network state (nodes/edges), research links, changes
8. **Frontend** (SimulationResults.vue) renders with Cytoscape.js graph

### Brain Network Model

**80 Brain Regions** seeded from `import.sql`:
- Core 10 regions: mPFC, PCC, Anterior Hippocampus, Amygdala, Visual Cortex, etc.
- Enhanced 70 regions: Hindbrain, Midbrain, Forebrain, Cortical areas
- Each region has: code, description, network assignment, neuroplasticity potential, baseline activity

**Neural Connections:**
- Weighted edges (0.1-1.0) representing functional connectivity
- Bidirectional connections (SimpleWeightedGraph)

**Compound Modifiers:**
- Hardcoded research-based effects for psilocybin, LSD, ketamine, MDMA
- Examples: psilocybin decreases DMN connectivity, MDMA enhances empathy circuits

**Setting Modifiers:**
- Calm-nature, guided-therapy, meditation-space, creative-studio, social-gathering
- Affect specific pathway groups (e.g., calm-nature boosts PFC-AMY)

**Duration Multipliers:**
- short: 0.7x, medium: 1.0x, extended: 1.3x

### Research Integration (PubMed)

**PubMed API Configuration** (`application.properties`):
- Base URL: `https://eutils.ncbi.nlm.nih.gov/entrez/eutils`
- Max results: 5 articles per simulation
- Relevance scoring weights: critical keywords (0.35), important (0.20), general (0.10)

**Keyword Extraction:**
SimulationService builds search queries from:
- Compound inspiration (e.g., "psilocybin")
- Therapeutic setting (e.g., "nature")
- Hardcoded neuroplasticity keywords

**Brain Region NLP/NER:**
- Extracts brain region mentions from article abstracts
- Uses alias mapping (e.g., "PFC", "prefrontal cortex", "medial prefrontal")
- Context validation to prevent false positives

## API Endpoints

Base path: `/api`

### Simulation
- `POST /api/simulations/run` - Run simulation with scenario
  - Request: `ScenarioRequest` (compoundInspiration, therapeuticSetting, integrationSteps, simulationDuration, userContext)
  - Response: `SimulationResponse` (networkState, mentionedRegions, researchArticles, changes, insights)

### User Progress
- `GET /api/users/{userId}/progress` - Get user progress
- `POST /api/users/{userId}/progress` - Update progress (quests, badges, level)

### Onboarding
- `POST /api/onboarding` - Submit onboarding quiz

### Brain Regions
- `GET /api/brain-regions` - Get all brain regions (for exploration)

## Testing

### Backend Tests
Located in `backend/src/test/java/com/synapsim/`:
- `BackendApplicationTests.java` - Application context test
- `service/` - Service layer tests (e.g., BrainNetworkServiceTest, PubMedServiceTest)

Run with: `./mvnw test`

### Frontend Tests
Test framework: Vitest with @vue/test-utils
Run with: `npm run test:unit`

## Important Implementation Notes

### Database Schema
- JPA entities use `@Entity`, `@Table`, Lombok annotations
- Relationships: `@ManyToOne`, `@OneToMany` for scenarios/simulations/references
- Simulation status enum: PROCESSING, COMPLETED, FAILED

### Graph Processing
- JGraphT `SimpleWeightedGraph<String, DefaultWeightedEdge>`
- Brain region names are graph vertices
- Neural connections are weighted edges
- Neuroplasticity changes modify edge weights

### Error Handling
- Custom exception: `NoResearchFoundException` when PubMed returns no results
- Frontend should gracefully handle this and prompt user to try different parameters

### CORS Configuration
Backend allows frontend connections from `http://localhost:5173` (Vite dev server)

### Environment Variables
Frontend uses Vite env vars:
- `VITE_API_BASE_URL` - Backend API URL (default: `http://localhost:8080/api`)

## Current Development State

**Completed:**
- Complete backend simulation engine with PubMed integration
- Enhanced brain region detection (NLP/NER)
- Frontend visualization with Cytoscape.js
- Database seeding with 80 brain regions
- Research-driven graph filtering

**Known Issues:**
- Some SQL parsing issues in `import.sql` (multi-row INSERT statements) - application works despite warnings

**Future Enhancements:**
- User authentication system
- Simulation history persistence per user
- Export simulation results (PDF/images)
- 3D visualization with Three.js
- Community sharing features

## Branch Information

Current development branch: `SS-7-brain-graph`
Main branch: `master`

## Documentation References

- **MVP Document:** `SynapSim MVP Document.md` - Full product requirements
- **Brain Areas Reference:** `Brain Areas and Networks.md` - Neuroscience background
- **Development Logs:** `session_logs/` - Detailed session notes
- **API Collection:** `SynapSim-API.postman_collection.json` - Postman collection for testing
