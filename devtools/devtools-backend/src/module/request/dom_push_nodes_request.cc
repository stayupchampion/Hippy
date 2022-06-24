/*
 * Tencent is pleased to support the open source community by making
 * Hippy available.
 *
 * Copyright (C) 2017-2019 THL A29 Limited, a Tencent company.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "module/request/dom_push_nodes_request.h"
#include "devtools_base/parse_json_util.h"

namespace hippy::devtools {

constexpr char kParamsBackendNodeIds[] = "backendNodeIds";

void DomPushNodesRequest::Deserialize(const std::string &params) {
  auto params_json = nlohmann::json::parse(params, nullptr, false);
  if (params_json.is_discarded()) {
    return;
  }
  backend_ids_ = TdfParseJsonUtil::GetJsonValue(params_json, kParamsBackendNodeIds, std::vector<int32_t>());
}

}  // namespace hippy::devtools