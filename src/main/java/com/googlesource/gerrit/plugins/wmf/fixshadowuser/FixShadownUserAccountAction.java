// Copyright (C) 2018 Wikimedia Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.wmf.fixshadowuser;

import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.server.account.AccountResource;
import com.google.gerrit.server.account.externalids.ExternalId;
import com.google.gerrit.server.account.externalids.ExternalIds;
import com.google.gerrit.server.account.externalids.ExternalIdsBatchUpdate;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import org.eclipse.jgit.errors.ConfigInvalidException;

@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
class FixShadowUserAccountAction
    implements RestModifyView<AccountResource, FixShadowUserAccountAction.Input> {

  private Provider<CurrentUser> self;
  private ExternalIds externalIds;
  private ExternalIdsBatchUpdate externalIdsBatchUpdate;

  static class Input {
    Integer correctaccountid;
  }

  @Inject
  FixShadowUserAccountAction(
      Provider<CurrentUser> self,
      ExternalIds externalids,
      ExternalIdsBatchUpdate externalIdsBatchUpdate) {
    this.self = self;
    this.externalIds = externalids;
    this.externalIdsBatchUpdate = externalIdsBatchUpdate;
  }

  /**
   * Fix ExternalId in the NoteDB database
   * @param extId the users current externalId to be updated
   * @param correctId the correct integer id of the user
   * @return String message for rest-api
   */
  private String fixExtId(ExternalId extId, String username, Integer correctId) {
    Account.Id id = new Account.Id(correctId);

    ExternalId extIdCorrected = ExternalId.create(
      ExternalId.SCHEME_USERNAME,
      username,
      id,
      extId.email(),
      extId.password()
    );

    this.externalIdsBatchUpdate.replace(extId, extIdCorrected);

    return String.format(
      "'%s:%s' accountId changed to %d",
      ExternalId.SCHEME_USERNAME,
      username,
      correctId
    );
  }

  @Override
  public String apply(AccountResource account, Input input) {
	try {
      ExternalId extId = this.externalIds.get(
        ExternalId.Key.create(ExternalId.SCHEME_USERNAME, account.getUser().getUserName())
      );
      if (extId == null) {
        return "User id not found in All-Users";
      }

      if (input.correctaccountid == null) {
          return "correctaccountid required";
      }

      return fixExtId(extId, account.getUser().getUserName(), input.correctaccountid);
    } catch (IOException|ConfigInvalidException e) {
      return "There was a problem :(";
    }
  }
}
