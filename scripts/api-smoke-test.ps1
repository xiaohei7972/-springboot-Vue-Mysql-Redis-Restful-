param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::new($Method),
        "$BaseUrl$Path")
    foreach ($key in $Headers.Keys) {
        [void]$request.Headers.TryAddWithoutValidation($key, [string]$Headers[$key])
    }
    if ($null -ne $Body) {
        $request.Content = [System.Net.Http.StringContent]::new(
            ($Body | ConvertTo-Json -Depth 8),
            [System.Text.Encoding]::UTF8,
            "application/json")
    }

    $client = [System.Net.Http.HttpClient]::new()
    try {
        $response = $client.SendAsync($request).Result
        $text = $response.Content.ReadAsStringAsync().Result
        $data = if ([string]::IsNullOrWhiteSpace($text)) { $null } else { $text | ConvertFrom-Json }
        return [pscustomobject]@{
            Status = [int]$response.StatusCode
            Data = $data
            Text = $text
        }
    } finally {
        $client.Dispose()
    }
}

function Assert-Equal {
    param($Actual, $Expected, [string]$Message)
    if ($Actual -ne $Expected) {
        throw "$Message; expected=$Expected actual=$Actual"
    }
}

function Login {
    param([string]$Username)
    $result = Invoke-Api POST "/api/auth/login" @{} @{
        username = $Username
        password = "123456"
    }
    Assert-Equal $result.Data.code 0 "$Username login failed"
    return @{ Authorization = "Bearer $($result.Data.data.token)" }
}

$unauthorized = Invoke-Api GET "/api/dashboard/summary"
Assert-Equal $unauthorized.Data.code 401 "unauthorized response"

$admin = Login "admin"
$teacher = Login "teacher01"
$student = Login "student01"

$departments = Invoke-Api GET "/api/departments" $admin
Assert-Equal $departments.Data.code 0 "department query"
if ($departments.Data.data.Count -lt 1) { throw "department query returned no records" }

$usersAsTeacher = Invoke-Api GET "/api/users?page=1&size=10" $teacher
Assert-Equal $usersAsTeacher.Data.code 403 "teacher user permission"

$adminCourses = Invoke-Api GET "/api/courses" $admin
$teacherCourses = Invoke-Api GET "/api/courses" $teacher
$studentCourses = Invoke-Api GET "/api/courses" $student
Assert-Equal $adminCourses.Data.code 0 "admin course query"
Assert-Equal $teacherCourses.Data.code 0 "teacher course query"
Assert-Equal $studentCourses.Data.code 0 "student course query"
if ($studentCourses.Data.data.Count -gt $adminCourses.Data.data.Count) {
    throw "student course scope is broader than admin scope"
}

$studentGrades = Invoke-Api GET "/api/grades" $student
$studentAttendance = Invoke-Api GET "/api/attendance" $student
Assert-Equal $studentGrades.Data.code 0 "student grade query"
Assert-Equal $studentAttendance.Data.code 0 "student attendance query"

Write-Output "API smoke test passed: $BaseUrl"
