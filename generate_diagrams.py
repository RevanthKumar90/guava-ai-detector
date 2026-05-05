import base64
import zlib
import urllib.request
import os

def kroki_encode(text):
    compressed = zlib.compress(text.encode('utf-8'))
    return base64.urlsafe_b64encode(compressed).decode('utf-8')

def download_diagram(name, text, diagram_type="mermaid"):
    encoded = kroki_encode(text)
    url = f"https://kroki.io/{diagram_type}/png/{encoded}"
    try:
        # User Agent is sometimes required by external APIs
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response:
            with open(f"diagrams/{name}.png", "wb") as f:
                f.write(response.read())
        print(f"Downloaded {name}.png")
    except Exception as e:
        print(f"Failed to download {name}: {e}")
        print(f"URL: {url}")

os.makedirs("diagrams", exist_ok=True)

mermaids = {
    "1_System_Architecture": """
graph TD
    A[User] -->|Input| B(Camera / Gallery)
    B --> C[MainActivity]
    C --> D{Image Validation}
    D -- Valid --> E[TensorFlow Lite Model]
    E --> F[ABIM Severity Analysis]
    F --> G[Recommendation Module]
    G --> H[Result Screen]
    H --> I[(History Storage)]
    """,
    "2_Use_Case": """
flowchart LR
    A((User))
    A --> UC1(Open Application)
    A --> UC2(Capture Leaf Image)
    A --> UC3(Select Image from Gallery)
    A --> UC4(Validate Guava Leaf)
    A --> UC5(Detect Disease)
    A --> UC6(View Severity)
    A --> UC7(Get Recommendation)
    A --> UC8(Save History)
    A --> UC9(View Previous Results)
    """,
    "3_Class_Diagram": """
classDiagram
    MainActivity --> ImageUtils
    MainActivity --> ModelHelper
    MainActivity --> ABIMUtils
    MainActivity --> RecommendationUtils
    MainActivity --> HistoryActivity
    class MainActivity{
        +onCreate()
        +processImage()
    }
    class ImageUtils{
        +isLeafImage()
        +isLowLight()
    }
    class ModelHelper{
        +predict()
    }
    class ABIMUtils{
        +generateOverlay()
    }
    class SeverityUtils{
        +getSeverity()
    }
    class RecommendationUtils{
        +getTreatment()
        +getPrevention()
    }
    class HistoryActivity{
        +onCreate()
    }
    """,
    "4_Sequence_Diagram": """
sequenceDiagram
    actor User
    participant MA as MainActivity
    participant CG as Camera/Gallery
    participant IU as ImageUtils
    participant MH as ModelHelper
    participant AU as ABIMUtils
    participant RU as RecommendationUtils

    User->>MA: Requests Action
    MA->>CG: Open Camera/Gallery
    CG-->>MA: Return Image/Frame
    MA->>IU: Validate Leaf
    IU-->>MA: Validation Result
    MA->>MH: Pass valid Image
    MH-->>MA: Inference Results
    MA->>AU: Calculate Severity
    AU-->>MA: Severity Data
    MA->>RU: Get Recommendations
    RU-->>MA: Treatments
    MA-->>User: Show Result Screen
    """,
    "5_Activity_Diagram": """
stateDiagram-v2
    [*] --> OpenApp
    OpenApp --> SelectOrCaptureImage
    SelectOrCaptureImage --> ValidateLeaf
    ValidateLeaf --> DiseaseDetection
    DiseaseDetection --> SeverityCalculation
    SeverityCalculation --> Recommendation
    Recommendation --> SaveResult
    SaveResult --> [*]
    """,
    "6_Flowchart_Diagram": """
flowchart TD
    Start([Start]) --> OpenApp[Open App]
    OpenApp --> InputImage[Choose Camera or Gallery -> Input Image]
    InputImage --> IsGuava{Is Guava Leaf?}
    IsGuava -- No --> ShowError[Show Error]
    IsGuava -- Yes --> RunMobileNet[Run MobileNetV2]
    RunMobileNet --> DetectDisease[Detect Disease]
    DetectDisease --> RunABIM[Run ABIM for Severity]
    RunABIM --> ShowResult[Show Result]
    ShowResult --> End([End])
    ShowError --> End
    """,
    "7_Component_Diagram": """
graph TD
    A[Android UI] --> B[Camera Module]
    A --> C[Gallery Module]
    A --> D[Disease Classification Module]
    A --> E[Severity Analysis Module]
    D --> F[TensorFlow Lite Engine]
    E --> G[ABIM Engine]
    A --> H[Recommendation Module]
    A --> I[(SQLite / Local Storage)]
    """,
    "8_Deployment_Diagram": """
graph TD
    subgraph Android Mobile Device
        App[Guava Leaf AI App]
        TFLite[TensorFlow Lite Model File .tflite]
        Storage[(Local Storage)]
        Offline[No Internet / Offline Mode]
    end
    App --> TFLite
    App --> Storage
    App ---> Offline
    """,
    "9_ER_Diagram": """
erDiagram
    ScanHistory {
        string ID
        string Date
        string Result
    }
    Disease {
        string Name
        string Description
    }
    Recommendation {
        string Treatment
        string Prevention
    }
    ScanHistory ||--|{ Disease : detects
    Disease ||--|{ Recommendation : has
    """,
    "10A_DFD_Level_0": """
flowchart LR
    User([User]) -->|Image Input| System[Detection System]
    System -->|Result + Recommendation| User
    """,
    "10B_DFD_Level_1": """
flowchart LR
    Img[Image Capture] --> Val[Validation]
    Val --> Pred[Disease Prediction]
    Pred --> Sev[Severity Estimation]
    Sev --> Rec[Recommendation Generation]
    """,
    "11_MobileNetV2_Working": """
flowchart LR
    A[Input Image] --> B[Resize]
    B --> C[Normalize]
    C --> D[MobileNetV2 Layers]
    D --> E[Output Class]
    """,
    "12_ABIM_Working": """
flowchart LR
    A[Disease Image] --> B[HSV Conversion]
    B --> C[Red Region Extraction]
    C --> D[Area Percentage]
    D --> E[Severity Level]
    """
}

for name, code in mermaids.items():
    download_diagram(name, code.strip())

print("All diagrams generated and saved to 'diagrams' directory.")
