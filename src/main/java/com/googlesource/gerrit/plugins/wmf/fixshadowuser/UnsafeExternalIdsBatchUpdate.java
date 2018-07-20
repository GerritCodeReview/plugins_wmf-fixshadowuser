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

import com.google.gerrit.server.account.externalids.ExternalId;
import com.google.gerrit.server.account.externalids.ExternalIdCache;
import com.google.gerrit.server.account.externalids.ExternalIdsBatchUpdate;
import com.google.gerrit.server.GerritPersonIdent;
import com.google.gerrit.server.config.AllUsersName;
import com.google.gerrit.server.extensions.events.GitReferenceUpdated;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jgit.lib.PersonIdent;

public class UnsafeExternalIdsBatchUpdate extends ExternalIdsBatchUpdate {
  private final Set<ExternalId> toAdd = new HashSet<>();
  private final Set<ExternalId> toDelete = new HashSet<>();

  @Inject
  public UnsafeExternalIdsBatchUpdate(
      GitRepositoryManager repoManager,
      GitReferenceUpdated gitRefUpdated,
      AllUsersName allUsersName,
      @GerritPersonIdent PersonIdent serverIdent,
      ExternalIdCache externalIdCache) {
    super(repoManager, gitRefUpdated, allUsersName, serverIdent, externalIdCache);
  }

  /**
   * Unsafely Adds an external ID replacement to the batch
   */
  @Override
  public void replace(ExternalId extIdToDelete, ExternalId extIdToAdd) {
    toAdd.add(extIdToAdd);
    toDelete.add(extIdToDelete);
  }
}
