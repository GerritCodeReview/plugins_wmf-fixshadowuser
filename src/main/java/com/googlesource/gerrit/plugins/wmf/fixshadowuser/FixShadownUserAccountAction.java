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
import com.google.gerrit.extensions.restapi.BadRequestException;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.server.account.AccountResource;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.account.AccountState;
import com.google.gerrit.server.account.externalids.ExternalId;
import com.google.gerrit.server.account.externalids.ExternalIds;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import org.eclipse.jgit.errors.ConfigInvalidException;

@RequiresCapability(GlobalCapability.ADMINISTRATE_SERVER)
class FixShadowUserAccountAction
    implements RestModifyView<AccountResource, FixShadowUserAccountAction.Input> {

  private Provider<CurrentUser> self;
  private ExternalIds externalIds;
  private UnsafeExternalIdsBatchUpdate unsafeExternalIdsBatchUpdate;
  private AccountCache accountCache;

  static class Input {
    Integer correctaccountid;
  }

  @Inject
  FixShadowUserAccountAction(
      Provider<CurrentUser> self,
      ExternalIds externalids,
      UnsafeExternalIdsBatchUpdate unsafeExternalIdsBatchUpdate,
      AccountCache accountCache) {
    this.self = self;
    this.externalIds = externalids;
    this.unsafeExternalIdsBatchUpdate = unsafeExternalIdsBatchUpdate;
    this.accountCache = accountCache;
  }

  /**
   * Assigning the username external ID to another account
   * @param extId the users current externalId to be updated
   * @param correctId the correct integer id of the user
   * @return String message for rest-api
   */
  private String reassignUsernameExternalIdAccount(ExternalId extId, Integer correctId) throws BadRequestException, IOException, OrmException, ConfigInvalidException {
    Account.Id id = new Account.Id(correctId);
    AccountState state = this.accountCache.get(id);

    if (state == null || !state.getAccount().isActive()) {
      throw new BadRequestException(
        String.format(
          "Account ID %d is not associated with any account",
          correctId
        )
      );
    }

    ExternalId extIdCorrected = ExternalId.create(
      ExternalId.SCHEME_USERNAME,
      extId.key().id(),
      id,
      extId.email(),
      extId.password()
    );

    this.unsafeExternalIdsBatchUpdate.replace(extId, extIdCorrected);
    this.unsafeExternalIdsBatchUpdate.commit("wmf-fixshadowuser update");

    return String.format(
      "'%s:%s' accountId changed to %d",
      ExternalId.SCHEME_USERNAME,
      extId.key().id(),
      correctId
    );
  }

  @Override
  public String apply(AccountResource account, Input input) throws ResourceConflictException, BadRequestException, IOException, OrmException, ConfigInvalidException {
    IdentifiedUser identifiedUser = account.getUser();
    String username = identifiedUser.getUserName();

    if (username == null) {
      return String.format(
        "Username for account %s not found!",
        account.getUser()
      );
    }

    ExternalId extId = this.externalIds.get(
      ExternalId.Key.create(ExternalId.SCHEME_USERNAME, username)
    );

    if (extId == null) {
      throw new ResourceConflictException(
        String.format(
          "External ID for username %s of account %s not found.",
          username,
          identifiedUser
        )
      );
    }

    if (input.correctaccountid == null) {
      throw new BadRequestException("correctaccountid required");
    }

    return reassignUsernameExternalIdAccount(extId, input.correctaccountid);
  }
}
