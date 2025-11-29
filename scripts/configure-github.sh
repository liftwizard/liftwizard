#!/usr/bin/env bash

set -Eeuo pipefail

# Configure GitHub repository settings
# This script sets up recommended settings for branch protection, merge options, and more

# Auto-detect repo and default branch from git remote
REPO=$(gh repo view --json nameWithOwner --jq '.nameWithOwner')
BRANCH=$(gh repo view --json defaultBranchRef --jq '.defaultBranchRef.name')

echo "Configuring GitHub settings for $REPO"
echo "Default branch: $BRANCH"
echo ""

# Helper function to prompt for confirmation
confirm() {
    local prompt="$1"
    local response
    read -r -p "$prompt [y/N] " response
    [[ "$response" =~ ^[Yy]$ ]]
}

# Helper to check and prompt for a boolean repo setting
check_repo_setting() {
    local setting="$1"
    local desired="$2"
    local description="$3"
    local current
    current=$(echo "$CURRENT_SETTINGS" | jq -r ".$setting")

    if [[ "$current" == "$desired" ]]; then
        return
    fi

    local action current_desc
    if [[ "$desired" == "true" ]]; then
        action="Enable"
        current_desc="currently disabled"
    else
        action="Disable"
        current_desc="currently enabled"
    fi

    if confirm "$action $description? ($current_desc)"; then
        gh api "repos/${REPO}" --method PATCH --field "$setting=$desired" > /dev/null
        echo "  Updated."
    fi
}

# ============================================================================
# Repository Settings
# ============================================================================

echo "=== Repository Settings ==="
echo ""

# Get current repo settings
CURRENT_SETTINGS=$(gh api "repos/${REPO}")

check_repo_setting "allow_squash_merge"      "false" "squash merging"
check_repo_setting "allow_merge_commit"      "true"  "merge commits"
check_repo_setting "allow_rebase_merge"      "true"  "rebase merging"
check_repo_setting "allow_auto_merge"        "true"  "auto-merge"
check_repo_setting "delete_branch_on_merge"  "true"  "delete branch on merge"
check_repo_setting "allow_update_branch"     "true"  "updating PR branches"

echo ""

# ============================================================================
# Branch Protection
# ============================================================================

echo "=== Branch Protection ($BRANCH) ==="
echo ""

# Check current branch protection
CURRENT_PROTECTION=$(gh api "repos/${REPO}/branches/${BRANCH}/protection" 2>/dev/null) || CURRENT_PROTECTION='{}'

# Get current values (with defaults for missing protection)
BP_CONTEXTS=$(echo "$CURRENT_PROTECTION" | jq -c '.required_status_checks.contexts // []')
BP_STRICT=$(echo "$CURRENT_PROTECTION" | jq -r '.required_status_checks.strict // false')
BP_LINEAR_HISTORY=$(echo "$CURRENT_PROTECTION" | jq -r '.required_linear_history.enabled // false')
BP_ALLOW_FORCE_PUSHES=$(echo "$CURRENT_PROTECTION" | jq -r '.allow_force_pushes.enabled // false')

UPDATE_PROTECTION=false

# Helper to check and prompt for branch protection boolean setting
check_protection_bool() {
    local var_name="$1"
    local desired="$2"
    local description="$3"
    local current="${!var_name}"

    if [[ "$current" == "$desired" ]]; then
        return
    fi

    local action current_desc
    if [[ "$desired" == "true" ]]; then
        action="Enable"
        current_desc="currently disabled"
    else
        action="Disable"
        current_desc="currently enabled"
    fi

    if confirm "$action $description? ($current_desc)"; then
        UPDATE_PROTECTION=true
        printf -v "$var_name" '%s' "$desired"
    fi
}

# Check required status checks (special case - not a simple bool)
if [[ "$BP_CONTEXTS" != '["All checks"]' ]]; then
    if confirm "Set required status checks to [\"All checks\"]? (currently $BP_CONTEXTS)"; then
        UPDATE_PROTECTION=true
        BP_CONTEXTS='["All checks"]'
        BP_STRICT=true
    fi
fi

check_protection_bool "BP_STRICT"             "true"  "require branches to be up to date"
check_protection_bool "BP_LINEAR_HISTORY"     "true"  "require linear history"
check_protection_bool "BP_ALLOW_FORCE_PUSHES" "true"  "allow force pushes (for admins)"

# Apply branch protection changes if any
if [[ "$UPDATE_PROTECTION" == "true" ]]; then
    echo "Updating branch protection..."

    # Preserve existing values for required fields we don't have opinions on
    BP_ENFORCE_ADMINS=$(echo "$CURRENT_PROTECTION" | jq -r '.enforce_admins.enabled // null')
    BP_REVIEWS=$(echo "$CURRENT_PROTECTION" | jq -c '.required_pull_request_reviews // null')
    BP_RESTRICTIONS=$(echo "$CURRENT_PROTECTION" | jq -c '.restrictions // null')

    cat << EOF | gh api "repos/${REPO}/branches/${BRANCH}/protection" --method PUT --input -
{
  "required_status_checks": {"strict": $BP_STRICT, "contexts": $BP_CONTEXTS},
  "enforce_admins": $BP_ENFORCE_ADMINS,
  "required_pull_request_reviews": $BP_REVIEWS,
  "restrictions": $BP_RESTRICTIONS,
  "required_linear_history": $BP_LINEAR_HISTORY,
  "allow_force_pushes": $BP_ALLOW_FORCE_PUSHES
}
EOF
    echo "  Branch protection updated."
fi

echo ""

# ============================================================================
# Security Settings
# ============================================================================

echo "=== Security Settings ==="
echo ""

check_security_setting() {
    local endpoint="$1"
    local description="$2"
    local current
    current=$(gh api "repos/${REPO}/$endpoint" --silent && echo "true" || echo "false")

    if [[ "$current" == "true" ]]; then
        return
    fi

    if confirm "Enable $description? (currently disabled)"; then
        gh api "repos/${REPO}/$endpoint" --method PUT
        echo "  Updated."
    fi
}

check_security_setting "vulnerability-alerts"      "vulnerability alerts"
check_security_setting "automated-security-fixes"  "automated security fixes (Dependabot)"

echo ""

# ============================================================================
# Summary
# ============================================================================

echo "=== Done ==="
