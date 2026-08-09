## 🌿 1. Branch Convention


| 브랜치 종류 | 설명 | Naming Format | Base Branch |
| :--- | :--- | :--- | :--- |
| **`main`** | Production 운영 서버 배포 브랜치 (CI/CD 자동 배포) | `main` | - |
| **`dev`** | 개발 integration 브랜치 (기본 Default Branch) | `dev` | `main` |
| **`feat`** | 새로운 기능 개발 | `feat/#이슈번호-기능명` | `dev` |
| **`fix`** | 버그 수정 | `fix/#이슈번호-수정내용` | `dev` |
| **`refactor`**| 코드 리팩토링 | `refactor/#이슈번호-내용` | `dev` |
| **`chore`** | 설정 파일 변경, 패키지 설치 등 | `chore/#이슈번호-내용` | `dev` |

### 🔄 Branch Workflow
1. Issue 발행 (`#이슈번호`)
2. `dev` 브랜치 기반으로 작업 브랜치 생성 (`git checkout -b feat/#1-login dev`)
3. 작업 및 커밋 진행
4. `dev` 브랜치 방향으로 **PR (Pull Request)** 생성
5. 코드 리뷰 완료 후 `dev`에 Merge
6. 배포 시점에 `dev` -> `main` PR 올린 후 Merge (CI/CD 트리거)

---

## 📝 2. Commit Message Convention

```text
[깃모지] Type: Subject (#이슈넘버)
```

- 🎉 **Start:** Start New Project [:tada:]
- ✨ **Feat:** 새로운 기능을 추가 [:sparkles:]
- 🐛 **Fix:** 버그 수정 [:bug:]
- 🎨 **Design:** CSS 등 사용자 UI 디자인 변경 [:art:]
- ♻️ **Refactor:** 코드 리팩토링 [:recycle:]
- 🔧 **Settings:** Changing configuration files [:wrench:]
- 🗃️ **Comment:** 필요한 주석 추가 및 변경 [:card_file_box:]
- ➕ **Dependency/Plugin:** Add a dependency/plugin [:heavy_plus_sign:]
- 📝 **Docs:** 문서 수정 [:memo:]
- 🔀 **Merge:** Merge branches [:twisted_rightwards_arrows:]
- 🚀 **Deploy:** Deploying stuff [:rocket:]
- 🚚 **Rename:** 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 [:truck:]
- 🔥 **Remove:** 파일을 삭제하는 작업만 수행한 경우 [:fire:]
- ⏪️ **Revert:** 전 버전으로 롤백 [:rewind:]

