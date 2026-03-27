#!/usr/bin/env python3
import json

def create_element(id, type, x, y, **kwargs):
    elem = {
        "id": id,
        "type": type,
        "x": x,
        "y": y,
        "angle": 0,
        "strokeColor": kwargs.get("strokeColor", "#1e1e1e"),
        "backgroundColor": kwargs.get("backgroundColor", "transparent"),
        "fillStyle": "hachure",
        "strokeWidth": 2,
        "strokeStyle": "solid",
        "roughness": kwargs.get("roughness", 1),
        "opacity": 100,
        "groupIds": [],
        "frameId": None,
        "roundness": None,
        "seed": 1,
        "version": 1,
        "versionNonce": 1,
        "isDeleted": False,
        "updated": 1,
        "link": None,
        "locked": False,
        "boundElements": None
    }

    # Add text-specific fields
    if type == "text":
        elem.update({
            "fontFamily": 1,
            "textAlign": "left",
            "verticalAlign": "top",
            "containerId": None,
            "originalText": kwargs.get("text", ""),
            "baseline": kwargs.get("fontSize", 14)
        })

    # Add arrow-specific fields
    if type == "arrow":
        width = kwargs.get("width", 0)
        height = kwargs.get("height", 0)
        elem.update({
            "points": [[0, 0], [width, height]],
            "lastCommittedPoint": None,
            "startBinding": None,
            "endBinding": None,
            "startArrowhead": None,
            "endArrowhead": "arrow"
        })

    elem.update(kwargs)
    return elem

elements = []

# Title
elements.append(create_element("title", "text", 400, 30,
    text="JTCool ERP 核心业务流程", fontSize=28, width=500, height=40))

# Order flow title
elements.append(create_element("order_title", "text", 80, 60,
    text="订单处理流程", fontSize=20, width=200, height=30))

# Order flow
elements.append(create_element("step1_box", "rectangle", 50, 100,
    width=180, height=60, backgroundColor="#a5d8ff", strokeColor="#1971c2"))
elements.append(create_element("step1_text", "text", 80, 120,
    text="客户下单", fontSize=16, width=120, height=25))

elements.append(create_element("arrow1", "arrow", 140, 160,
    width=0, height=50, strokeColor="#2f9e44"))

elements.append(create_element("step2_box", "rectangle", 50, 230,
    width=180, height=60, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("step2_text", "text", 70, 250,
    text="订单系统创建订单", fontSize=14, width=140, height=25))

elements.append(create_element("arrow2", "arrow", 140, 290,
    width=0, height=50, strokeColor="#2f9e44"))

elements.append(create_element("decision1", "diamond", 80, 360,
    width=120, height=80, backgroundColor="#fff3bf", strokeColor="#f59f00"))
elements.append(create_element("decision1_text", "text", 100, 390,
    text="库存充足?", fontSize=14, width=80, height=25))

elements.append(create_element("arrow3_yes", "arrow", 200, 400,
    width=80, height=0, strokeColor="#2f9e44"))
elements.append(create_element("yes_label", "text", 210, 380,
    text="是", fontSize=12, width=20, height=20))

elements.append(create_element("arrow3_no", "arrow", 140, 440,
    width=0, height=50, strokeColor="#c92a2a"))
elements.append(create_element("no_label", "text", 150, 455,
    text="否", fontSize=12, width=20, height=20))

elements.append(create_element("step3_box", "rectangle", 300, 370,
    width=160, height=60, backgroundColor="#d0bfff", strokeColor="#7950f2"))
elements.append(create_element("step3_text", "text", 320, 390,
    text="仓库系统锁定库存", fontSize=14, width=120, height=25))

elements.append(create_element("cancel_box", "rectangle", 60, 510,
    width=160, height=60, backgroundColor="#ffc9c9", strokeColor="#c92a2a"))
elements.append(create_element("cancel_text", "text", 80, 530,
    text="订单取消/待补货", fontSize=14, width=120, height=25))

elements.append(create_element("arrow4", "arrow", 380, 430,
    width=0, height=50, strokeColor="#2f9e44"))

elements.append(create_element("step4_box", "rectangle", 300, 500,
    width=160, height=60, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("step4_text", "text", 330, 520,
    text="生成出库单", fontSize=14, width=100, height=25))

elements.append(create_element("arrow5", "arrow", 380, 560,
    width=0, height=50, strokeColor="#2f9e44"))

elements.append(create_element("step5_box", "rectangle", 300, 630,
    width=160, height=60, backgroundColor="#d0bfff", strokeColor="#7950f2"))
elements.append(create_element("step5_text", "text", 330, 650,
    text="仓库拣货发货", fontSize=14, width=100, height=25))

elements.append(create_element("arrow6", "arrow", 380, 690,
    width=0, height=50, strokeColor="#2f9e44"))

elements.append(create_element("step6_box", "rectangle", 300, 760,
    width=160, height=60, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("step6_text", "text", 330, 780,
    text="订单完成", fontSize=14, width=100, height=25))

# AI flow title
elements.append(create_element("ai_title", "text", 620, 60,
    text="AI 助手查询流程", fontSize=20, width=200, height=30))

# AI flow
elements.append(create_element("ai1_box", "rectangle", 600, 100,
    width=180, height=60, backgroundColor="#a5d8ff", strokeColor="#1971c2"))
elements.append(create_element("ai1_text", "text", 650, 120,
    text="用户提问", fontSize=16, width=80, height=25))

elements.append(create_element("ai_arrow1", "arrow", 690, 160,
    width=0, height=50, strokeColor="#1971c2"))

elements.append(create_element("ai2_box", "rectangle", 600, 230,
    width=180, height=60, backgroundColor="#d0bfff", strokeColor="#7950f2"))
elements.append(create_element("ai2_text", "text", 630, 250,
    text="AI 安全网关检测", fontSize=14, width=120, height=25))

elements.append(create_element("ai_arrow2", "arrow", 690, 290,
    width=0, height=50, strokeColor="#7950f2"))

elements.append(create_element("ai3_box", "rectangle", 600, 360,
    width=180, height=60, backgroundColor="#ffc9c9", strokeColor="#c92a2a"))
elements.append(create_element("ai3_text", "text", 640, 380,
    text="RAG 向量检索", fontSize=14, width=100, height=25))

elements.append(create_element("ai_arrow3", "arrow", 690, 420,
    width=0, height=50, strokeColor="#c92a2a"))

elements.append(create_element("ai4_box", "rectangle", 600, 490,
    width=180, height=60, backgroundColor="#ffc9c9", strokeColor="#c92a2a"))
elements.append(create_element("ai4_text", "text", 640, 510,
    text="LLM 生成回答", fontSize=14, width=100, height=25))

elements.append(create_element("ai_arrow4", "arrow", 690, 550,
    width=0, height=50, strokeColor="#c92a2a"))

elements.append(create_element("ai5_box", "rectangle", 600, 620,
    width=180, height=60, backgroundColor="#d0bfff", strokeColor="#7950f2"))
elements.append(create_element("ai5_text", "text", 640, 640,
    text="输出脱敏处理", fontSize=14, width=100, height=25))

elements.append(create_element("ai_arrow5", "arrow", 690, 680,
    width=0, height=50, strokeColor="#7950f2"))

elements.append(create_element("ai6_box", "rectangle", 600, 750,
    width=180, height=60, backgroundColor="#b2f2bb", strokeColor="#2f9e44"))
elements.append(create_element("ai6_text", "text", 630, 770,
    text="返回结果给用户", fontSize=14, width=120, height=25))

# Create Excalidraw file
excalidraw_data = {
    "type": "excalidraw",
    "version": 2,
    "source": "https://excalidraw.com",
    "elements": elements,
    "appState": {
        "gridSize": None,
        "viewBackgroundColor": "#ffffff"
    },
    "files": {}
}

output_file = "jtcool-business-flow.excalidraw"
with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(excalidraw_data, f, ensure_ascii=False, indent=2)

print(f"Business flow diagram created: {output_file}")
